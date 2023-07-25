import request from '@/utils/request.ts'
export const airLinkSet = (data: any):Promise<any> => {
    return request({
        url: '/Device/setAir',
        data,
        method: 'POST'
    })
}

export const hardWareGet = ():Promise<any> => {
    return request({
        url: '/NettyApplication/hard-ware/get',
        method: 'get'
    })
}

export const DeviceInfoList = ():Promise<any> => {
    return request({
        url: '/DeviceInfo/list',
        method: 'get'
    })
}


