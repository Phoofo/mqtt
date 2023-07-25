package com.NettyApplication.service.impl;

import com.NettyApplication.entity.DeviceInfo;
import com.NettyApplication.mapper.DeviceInfoMapper;
import com.NettyApplication.service.IDeviceInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfo> implements IDeviceInfoService {
    
}
