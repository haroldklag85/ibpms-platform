import { unref, MaybeRef } from 'vue';

export enum UrgencyType {
    GREEN = 'GREEN',
    YELLOW = 'YELLOW',
    ORANGE = 'ORANGE', // @Traceability: US-001 - CA-6
    RED = 'RED',
    BLACK = 'BLACK' // Expirado (SLA Ruptured)
}

export function useSlaEngine() {
    /**
     * Calcula la urgencia de acuerdo al tiempo actual y la fecha de expiración SLA.
     * @Traceability: US-001 - CA-5, CA-11, CA-24
     * 
     * @param slaExpirationDate ISO string de resolución target
     * @param creationDate ISO string de inicio de tarea
     * @param currentTick Timestamp reactivo in-memory o epoch actual en ms
     */
    const calculateUrgency = (slaExpirationDate: MaybeRef<string>, creationDate: MaybeRef<string>, currentTick: MaybeRef<number>): UrgencyType => {
        const expir = new Date(unref(slaExpirationDate)).getTime();
        const start = new Date(unref(creationDate)).getTime();
        const now = unref(currentTick);

        if (now >= expir) return UrgencyType.BLACK;

        const totalDuration = expir - start;
        const remaining = expir - now;
        
        // Corrupted dates fallback
        if (totalDuration <= 0) return UrgencyType.BLACK;

        const percentageRemaining = (remaining / totalDuration) * 100;

        if (percentageRemaining <= 15) return UrgencyType.RED;
        if (percentageRemaining <= 30) return UrgencyType.ORANGE;
        if (percentageRemaining <= 70) return UrgencyType.YELLOW;
        
        return UrgencyType.GREEN;
    };

    return {
        calculateUrgency
    };
}
