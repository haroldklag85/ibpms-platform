import { ref, onMounted, onUnmounted } from 'vue';

export interface UseLazyLoadOptions {
  threshold?: number;
  rootMargin?: string;
}

export function useLazyLoad(options: UseLazyLoadOptions = {}) {
  const targetRef = ref<HTMLElement | null>(null);
  const isVisible = ref(false);
  let observer: IntersectionObserver | null = null;

  onMounted(() => {
    observer = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting) {
        isVisible.value = true;
        if (targetRef.value) {
          observer?.unobserve(targetRef.value);
        }
      }
    }, {
      threshold: options.threshold ?? 0,
      rootMargin: options.rootMargin ?? '0px'
    });

    if (targetRef.value) {
      observer.observe(targetRef.value);
    }
  });

  onUnmounted(() => {
    if (observer) {
      observer.disconnect();
    }
  });

  return {
    targetRef,
    isVisible
  };
}
