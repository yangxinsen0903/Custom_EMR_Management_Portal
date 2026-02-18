import {ref, reactive, defineEmits, defineProps, toRefs} from 'vue'

const pageTitle = {
  title: ref(''),
  setTitle(str) {
    pageTitle.title.value = str
  }
}

export default pageTitle