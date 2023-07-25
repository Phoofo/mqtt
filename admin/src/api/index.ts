import request from '@/utils/request.ts'
export const airLinkSet = (data: any):Promise<any> => {
    return request({
        url: '/airLink/set',
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


