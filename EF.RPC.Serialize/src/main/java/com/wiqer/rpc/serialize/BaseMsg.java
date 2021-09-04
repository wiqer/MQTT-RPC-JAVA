package com.wiqer.rpc.serialize;

import com.wiqer.rpc.serialize.utils.EFNETID;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseMsg {
    public BaseMsg()
    {
        Id = EFNETID.getSnowflake64IdWithCount();
        CreationDate =LocalDateTime.now();
    }
    public BaseMsg(String id, LocalDateTime createDate)
    {
        Id = id;
        CreationDate = createDate;
    }
    public String Id ;
     
    public LocalDateTime CreationDate ;

}
