const dev = process.env.NODE_ENV !== 'production'

const config = {
  // baseURL: dev ? 'http://172.179.16.63:8081' : '',
  baseURL: dev ? '' : '',
  aesKey: 'm1HR8rySqB3WUFCw'
}

export default config
