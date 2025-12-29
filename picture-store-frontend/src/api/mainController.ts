// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** getHealthTest GET /api/health */
export async function getHealthTestUsingGet(options?: { [key: string]: any }) {
  return request<string>('/api/health', {
    method: 'GET',
    ...(options || {}),
  })
}
