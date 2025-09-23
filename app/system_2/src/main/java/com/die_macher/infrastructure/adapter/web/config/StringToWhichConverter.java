package com.die_macher.infrastructure.adapter.web.config;

import com.die_macher.infrastructure.adapter.web.dto.TomorrowDataRequest.Which;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToWhichConverter implements Converter<String, Which> {

  @Override
  public Which convert(String source) {
    return Which.fromValue(source);
  }
}
