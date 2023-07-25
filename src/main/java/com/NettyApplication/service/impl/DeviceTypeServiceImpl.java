package com.NettyApplication.service.impl;

import com.NettyApplication.entity.DeviceType;
import com.NettyApplication.mapper.DeviceTypeMapper;
import com.NettyApplication.service.IDeviceTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@Service
public class DeviceTypeServiceImpl extends ServiceImpl<DeviceTypeMapper, DeviceType> implements IDeviceTypeService {
    
}
