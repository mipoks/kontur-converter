package design.kfu.converter.controller;

import design.kfu.converter.dto.ConvertDto;
import design.kfu.converter.service.ConvertService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
public class ConvertController {

    @Autowired
    ConvertService converterService;

    @PostMapping("/convert")
    public String index(@RequestBody ConvertDto convertInfo, BindingResult result, HttpServletResponse response) {
        if (!result.hasErrors()) {
            try {
                return converterService.convert(convertInfo);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                response.setStatus(400);
            } catch (IllegalStateException e) {
                response.setStatus(404);
            }
        } else {
            response.setStatus(404);
        }
        return "";
    }

}
