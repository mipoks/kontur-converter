package design.kfu.converter.service;


import design.kfu.converter.dto.ConvertDto;

public interface ConvertService {
    String convert(ConvertDto convertDto) throws IllegalStateException, IllegalArgumentException;
}
