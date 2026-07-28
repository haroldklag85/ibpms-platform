import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { useSoftUndo } from '@/composables/useSoftUndo';

describe('useSoftUndo', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('execute() inicia countdown y ejecuta actionFn() despues de timeoutMs', () => {
    const actionFn = vi.fn();
    const { execute, isUndoing, countdown } = useSoftUndo(actionFn, { timeoutMs: 5000 });

    execute();
    expect(isUndoing.value).toBe(true);
    expect(countdown.value).toBe(5);

    vi.advanceTimersByTime(2000);
    expect(countdown.value).toBe(3);
    expect(actionFn).not.toHaveBeenCalled();

    vi.advanceTimersByTime(3000);
    expect(isUndoing.value).toBe(false);
    expect(countdown.value).toBe(0);
    expect(actionFn).toHaveBeenCalledOnce();
  });

  it('cancel() antes del timeout cancela la ejecucion', () => {
    const actionFn = vi.fn();
    const { execute, cancel, isUndoing } = useSoftUndo(actionFn, { timeoutMs: 5000 });

    execute();
    expect(isUndoing.value).toBe(true);

    vi.advanceTimersByTime(2000);
    cancel();

    expect(isUndoing.value).toBe(false);

    vi.advanceTimersByTime(3000);
    expect(actionFn).not.toHaveBeenCalled();
  });

  it('Limpia el estado y timers correctamente', () => {
    const actionFn = vi.fn();
    const { execute, cancel, isUndoing, countdown } = useSoftUndo(actionFn);

    execute();
    cancel();
    expect(isUndoing.value).toBe(false);
    expect(countdown.value).toBe(0);
  });
});
