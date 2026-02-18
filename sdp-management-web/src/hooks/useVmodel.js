import { ref, watch, getCurrentInstance, computed } from "vue";
// export function useVmodel(props, key = "modelValue", emit) {
//   const vm = getCurrentInstance();
//   const _emit = emit || vm?.emit;
//   const event = `update:${key}`;
//   const proxy = ref(props[key]);
//   watch(
//     () => proxy.value,
//     (v) => _emit(event, v)
//   );
//   return proxy;
// }

export function useVmodel(props, key = "modelValue", emit) {
  const vm = getCurrentInstance();
  const _emit = emit || vm?.emit;
  const event = `update:${key}`;
  const proxy = computed({
    get: () => props[key],
    set: (val) => {
      _emit(event, val)
    }
  })
  return proxy;
}