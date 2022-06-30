package com.example.emos.wx.config.xss;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import cn.hutool.json.JSONUtil;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;


/**
 * 定义wrapper类通过filter拦截数据，将数据转义，防止xss攻击
 *
 * @author 2657944563
 */

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getParameter(String name) {
        String str = super.getParameter(name);
        if (!StrUtil.isEmpty(str)) {
            str = HtmlUtil.filter(str);
        }
        return str;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (!parameterMap.isEmpty()) {
            for (String s : parameterMap.keySet()) {
                String[] strings = parameterMap.get(s);
                if (!StrUtil.hasEmpty(strings)) {
                    for (int i = 0; i < strings.length; i++) {
                        strings[i] = HtmlUtil.filter(strings[i]);
                    }
                }
                parameterMap.put(s, strings);
            }
        }
        return parameterMap;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] parameterValues = super.getParameterValues(name);
        if (!StrUtil.hasEmpty(parameterValues)) {
            for (int i = 0; i < parameterValues.length; i++) {
                if (!StrUtil.isEmpty(parameterValues[i])) {
                    parameterValues[i] = HtmlUtil.filter(parameterValues[i]);
                }
            }
        }
        return parameterValues;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        ServletInputStream inputStream = super.getInputStream();
        InputStreamReader in = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(in);
        StringBuilder stringBuilder = new StringBuilder();
        String str = bufferedReader.readLine();
        while (str != null) {
            stringBuilder.append(str);
            str = bufferedReader.readLine();
        }
        bufferedReader.close();
        in.close();
        inputStream.close();
        Map<String, Object> map = JSONUtil.parseObj(stringBuilder.toString());
        if (!map.isEmpty()) {
            for (String s : map.keySet()) {
                Object str1 = map.get(s);
                if (str1 instanceof String) {
                    map.put(s, HtmlUtil.filter(str1.toString()));
                }
            }
        }
        String s = JSONUtil.toJsonStr(map);
        InputStream input = new ByteArrayInputStream(s.getBytes());
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() throws IOException {
                return input.read();
            }
        };
    }
}
