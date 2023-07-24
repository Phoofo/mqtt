package com.NettyApplication.service;

import com.NettyApplication.entity.HardWare;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
public interface IHardWareService extends IService<HardWare> {

    List<Map<String,String>> selectActivitiesByUserId();

}
