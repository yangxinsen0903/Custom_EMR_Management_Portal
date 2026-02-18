import moment from "moment"
moment.locale('zh-cn');

export function formatTime(time) {
  let timeStr = moment.utc(time).local().format('YYYY-MM-DD HH:mm:ss')
  if (timeStr == 'Invalid date') {
    return '-'
  }
  return timeStr
}

export function formatTimeYMD(time) {
  let timeStr = moment.utc(time).local().format('YYYY-MM-DD')
  if (timeStr == 'Invalid date') {
    return '-'
  }
  return timeStr
}
export function formatTimeMD(time) {
  let timeStr = moment.utc(time).local().format('MM-DD')
  if (timeStr == 'Invalid date') {
    return '-'
  }
  return timeStr
}

export function formatTimeHM(time) {
  let timeStr = moment.utc(time).local().format('HH:mm')
  if (timeStr == 'Invalid date') {
    return '-'
  }
  return timeStr
}

export function columnTimeFormat(row, column) {
  let time = row[column.property]
  return formatTime(time)
}

export function timeToUtcTime(time) {
  let timeStr = moment(time).utc().format('YYYY-MM-DD HH:mm:ss')
  if (timeStr == 'Invalid date') {
    return ''
  }
  return timeStr
}

export function getLocalTimeStr(){
  let timeStr = moment.utc(new Date(),'YYYY-MM-DD HH:mm:ss').local().format('YYYY-MM-DD HH:mm:ss')
  if (timeStr == 'Invalid date') {
    return ''
  }
  return timeStr
}

