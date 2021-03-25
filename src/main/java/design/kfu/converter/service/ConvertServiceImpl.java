package design.kfu.converter.service;

import design.kfu.converter.dto.ConvertDto;
import design.kfu.converter.helper.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConvertServiceImpl implements ConvertService {

    @Autowired
    private Converter converter;

    @Override
    public String convert(ConvertDto convertDto) throws IllegalStateException, IllegalArgumentException {
        if (!converter.validateFromAndTo(convertDto.getTo()) || !converter.validateFromAndTo(convertDto.getFrom())) {
            throw new IllegalArgumentException("No rule for converting");
        }
        return converter.format15num(converter.convert(convertDto.getFrom(), convertDto.getTo()));
    }
}
