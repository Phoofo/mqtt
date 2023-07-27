import request from '@/utils/request.ts'
export const airLinkSet = (data: any):Promise<any> => {
    return request({
        url: '/Device/setAir',
        data,
        method: 'POST'
    })
}

export const hardWareGet = (params?: object):Promise<any> => {
    return request({
        url: '/NettyApplication/hard-ware/get',
        method: 'get',
        params
    })
}

export const DeviceInfoList = (params?: object):Promise<any> => {
    return request({
        url: '/DeviceInfo/list',
        method: 'get',
        params
    })
}
/**
 * @description: 获取主板信息
 * @fileName: index.ts 
 * @author: snow_yp
 * @date: 2023-07-26 15:09:58
 */
export const listControl = (params?: object):Promise<any> => {
    return request({
        url: '/DeviceInfo/listControl',
        method: 'get',
        params
    })
}
/**
 * @description: 修改信息
 * @fileName: index.ts 
 * @author: snow_yp
 * @date: 2023-07-26 16:01:20
 */
export const configurationLocation = (data?: object):Promise<any> => {
    return request({
        url: '/DeviceInfo/configurationLocation',
        method: 'post',
        data
    })
}

/**
 * @description: 查看主板下的设备列表
 * @fileName: index.ts 
 * @author: snow_yp
 * @date: 2023-07-26 16:09:24
 */
export const listByControl = (params?: object):Promise<any> => {
    return request({
        url: '/DeviceInfo/listByControl',
        method: 'get',
        params
    })
}
/**
 * @description: 主板一键操作
 * @fileName: index.ts 
 * @author: snow_yp
 * @date: 2023-07-27 09:39:45
 */
export const setAirBatch = (data?: object):Promise<any> => {
    return request({
        url: '/Device/setAirBatch',
        method: 'post',
        data
    })
}








