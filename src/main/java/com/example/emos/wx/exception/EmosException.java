package com.example.emos.wx.exception;

import lombok.Data;

@Data
//@ControllerAdvice
public class EmosException extends RuntimeException {
    private String msg;
    private int code = 500;

    EmosException(String msg) {
        super(msg);
        this.msg = msg;
    }

    EmosException(String msg, Throwable throwable) {
        super(msg, throwable);
        this.msg = msg;
    }

    EmosException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    EmosException(String msg, int code, Throwable throwable) {
        super(msg, throwable);
        this.code = code;
        this.msg = msg;
    }

//    @ExceptionHandler
//    public ModelAndView handleException(IllegalAccessException e) {
//        ModelAndView modelAndView = new ModelAndView("error");
//        modelAndView.addObject("errorMessage", new EmosException("页面错误", 403));
//        return modelAndView;
//    }
}
