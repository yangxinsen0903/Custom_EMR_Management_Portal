import Clipboard from 'clipboard' //引入cliboard
import { ElMessage } from 'element-plus'  //消息提示


export default function handleClipboard(text, callback) {
  const clipboard = new Clipboard(event.target, {
    text: () => text,
  })
  clipboard.on('success', () => {
    callback(true)
    clipboard.destroy()
  })
  clipboard.on('error', () => {
    callback(false)
    clipboard.destroy()
  })
  clipboard.onClick(event)
}
