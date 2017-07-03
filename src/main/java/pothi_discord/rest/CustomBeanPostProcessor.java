package pothi_discord.rest;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestHeaderMapMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Map;

/**
 * Created by Pascal Pothmann on 03.07.2017.
 */
public class CustomBeanPostProcessor implements BeanPostProcessor{
    @Override
    public Object postProcessBeforeInitialization(Object bean, String s) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String s) throws BeansException {
        if(bean instanceof RequestHeaderMapMethodArgumentResolver) {
            return new RequestHeaderMapMethodArgumentResolver() {
                @Override
                public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                    return new CaseInsensitiveMap((Map)super.resolveArgument(parameter, mavContainer, webRequest, binderFactory));
                }
            };
        } else
            return bean;
    }
}
