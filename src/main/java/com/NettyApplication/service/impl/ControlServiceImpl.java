package com.NettyApplication.service.impl;

import com.NettyApplication.entity.Control;
import com.NettyApplication.mapper.ControlMapper;
import com.NettyApplication.service.IControlService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@Service
public class ControlServiceImpl extends ServiceImpl<ControlMapper, Control> implements IControlService {

    @Override
    @Transactional
    public void configurationLocation(Control control) {
//        Assert.notNull(control.getAddress(), "位置信息不能为空");
        Assert.notNull(control.getId(), "主控板信息不能为空");
        Control control1 = baseMapper.selectById(control.getId());
        control1.setAddress(control.getAddress());
        baseMapper.updateById(control1);
    }
}
