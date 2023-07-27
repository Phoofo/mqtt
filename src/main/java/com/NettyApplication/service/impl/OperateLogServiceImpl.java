package com.NettyApplication.service.impl;

import com.NettyApplication.entity.OperateLog;
import com.NettyApplication.mapper.OperateLogMapper;
import com.NettyApplication.service.IOperateLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
@Service
public class OperateLogServiceImpl extends ServiceImpl<OperateLogMapper, OperateLog> implements IOperateLogService {

}
