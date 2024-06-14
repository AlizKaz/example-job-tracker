package com.thirty3.job.job_tracker.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.Date;

public class DateToEpochSeconds extends StdSerializer<Date> {
  public DateToEpochSeconds() {
    this(null);
  }

  public DateToEpochSeconds(Class<Date> date) {
    super(date);
  }

  @Override
  public void serialize(Date val, JsonGenerator gen, SerializerProvider ser) throws IOException {
    gen.writeNumber(val.getTime());
  }
}
