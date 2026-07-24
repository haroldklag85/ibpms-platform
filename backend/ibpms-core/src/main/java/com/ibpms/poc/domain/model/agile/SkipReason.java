package com.ibpms.poc.domain.model.agile;

/**​‍​​‍​​​​‍​​​​​‍​‍​‍​​‍​​‍​​‍‍‍‍​‍​​‍‍​​​‍​​​‍​​​​‍​‍‍​‍​‍​​​‍‍‍​‍​​‍‍‍‍​‍​​‍‍​‍​‍​​​‍​‍​‍​‍‍​‍​​​‍​‍‍​‍​‍​​‍​​‍​‍​​​​‍​​‍​‍​​​​​‍​​‍‍​‍​‍​‍​​‍‍​​‍​‍‍​‍​​‍‍​​‍​​​‍‍​​​​​​‍‍​​‍​​​‍‍​‍‍​
 * Motivos de skipeo conformes al Journey J-04 v2 (CU-J04-25 a 28).
 * Alineados con docs/uat/casos_uso_uat_j04.md §F5.
 */
public enum SkipReason {
    CLIENT_NO_RESPONSE,
    REQUIRES_DOCUMENTATION,
    OUT_OF_AREA,
    OTHER
}
