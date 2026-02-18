import moment from "moment"
moment.locale('zh-cn');

export const shortcuts = [
  {
    text: '今天',
    value: () => {
      const end = new Date()
      const start = moment().startOf('day').valueOf()
      return [start, end]
    },
  },
  {
    text: '3天内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 2)
      return [start, end]
    },
  },
  {
    text: '7天内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 6)
      return [start, end]
    },
  },
  {
    text: '30天内',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 29)
      return [start, end]
    },
  },
]

