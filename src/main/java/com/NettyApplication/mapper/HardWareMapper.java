package com.NettyApplication.mapper;

import com.NettyApplication.entity.HardWare;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author yb
 * @since 2023-07-23
 */
public interface HardWareMapper extends BaseMapper<HardWare> {

    List<Map<String,String>> selectActivitiesByUserId();

}
