package com.wiqer.rpc.impl.core;

import lombok.Data;


import java.lang.reflect.Method;
import java.lang.reflect.Type;
@Data
public class MsgFun {
    public Method methodInfo ;
    public String packageName ;
    public String FullName ;
    public String ReqFullName ;
    public String className ;
    public String Name ;
    public Type responseType ;
    public Type[] reqTypes ;
}
