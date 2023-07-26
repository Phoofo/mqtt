import axios from "axios";
import { ElLoading } from 'element-plus'

const request = axios.create({
    baseURL: import.meta.env.VITE_BASE_API,
    timeout: 10000
})
let loading: any = null
request.interceptors.request.use(function (config) {
    loading = ElLoading.service({
        lock: true,
        text: 'Loading',
        background: 'rgba(0, 0, 0, 0.7)',
    })
    // 在发送请求之前做些什么
    return config;
}, function (error) {
    // 对请求错误做些什么
    loading && loading.close()
    return Promise.reject(error);
});

// 添加响应拦截器
request.interceptors.response.use(function (response) {
    // 2xx 范围内的状态码都会触发该函数。
    // 对响应数据做点什么    
    loading && loading.close()
    return Promise.resolve(response.data);
}, function (error) {
    // 超出 2xx 范围的状态码都会触发该函数。
    // 对响应错误做点什么
    loading && loading.close()
    return Promise.reject(error);
});

export default request