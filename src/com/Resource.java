package com;

import java.net.URL;

/**
 * @Author Xps13
 * @Description //TODO
 * @Date 2019/5/16 19:47
 */
public class Resource {

    public static URL getResource(String resourcePath){
        return Resource.class.getResource("../"+resourcePath);
    }
}
