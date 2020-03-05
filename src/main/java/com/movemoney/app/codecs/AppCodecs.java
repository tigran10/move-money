package com.movemoney.app.codecs;

import com.google.common.reflect.TypeToken;
import com.movemoney.app.dto.AccountData;
import com.movemoney.app.dto.MoveMoneyInstruction;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

public class AppCodecs {

    public static abstract class FasterXmlBasedCodec<T> implements MessageCodec<T, T> {

        @Override
        public void encodeToWire(Buffer buffer, T instruction) {
            String jsonToStr = Json.encode(instruction);
            int length = jsonToStr.getBytes().length;
            buffer.appendInt(length);
            buffer.appendString(jsonToStr);
        }

        @Override
        public T decodeFromWire(int position, Buffer buffer) {
            var typeToken = new TypeToken<T>(getClass()) {
            };

            int _pos = position;
            int length = buffer.getInt(_pos);

            return (T) Json.decodeValue(
                    buffer.getString(_pos += 4, _pos += length),
                    typeToken.getRawType());
        }

        @Override
        public T transform(T instruction) {
            return instruction;
        }

        @Override
        public String name() {
            return this.getClass().getSimpleName();
        }

        @Override
        public byte systemCodecID() {
            return -1;
        }
    }

    public static class AccountDataCodec extends FasterXmlBasedCodec<AccountData> { }

    public static class TransferInstructionCodec extends FasterXmlBasedCodec<MoveMoneyInstruction> { }


}
