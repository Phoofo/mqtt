package com.NettyApplication.service.impl;

import com.NettyApplication.entity.HardWare;
import com.NettyApplication.entity.HardWareControl;
import com.NettyApplication.mapper.HardWareControlMapper;
import com.NettyApplication.mapper.HardWareMapper;
import com.NettyApplication.service.IHardWareService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
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
public class HardWareServiceImpl extends ServiceImpl<HardWareMapper, HardWare> implements IHardWareService {
    @Autowired
    HardWareMapper hardWareMapper;
    @Autowired
    HardWareControlMapper hardWareControlMapper;

    @Override
    public List<Map<String,String>>   selectActivitiesByUserId() {

//        List<HardWareControl> hardWareControls = hardWareControlMapper.selectList(null);
//
//        List<HardWare> hardWares = hardWareMapper.selectList(null);
//        HashMap<Object, Object> objectObjectHashMap = new HashMap<>();
//        objectObjectHashMap.put("objectObjectHashMap",hardWareControls);
//        objectObjectHashMap.put("hardWares",hardWares);
        return hardWareMapper.selectActivitiesByUserId();
    }
}
