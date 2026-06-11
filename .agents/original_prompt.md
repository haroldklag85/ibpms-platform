## 2026-06-10T19:58:54Z

<user_information>
The USER's OS version is windows.
The user has 1 active workspaces, each defined by a URI and a CorpusName. Multiple URIs potentially map to the same CorpusName. The mapping is shown as follows in the format [URI] -> [CorpusName]:
z:\home\haroltandrsgmezagu\proyectos\ibpms-platform -> haroldklag85/ibpms-platform
Code relating to the user's requests should be written in the locations listed above. Avoid writing project code files to tmp, in the .gemini dir, or directly to the Desktop and similar folders unless explicitly asked.
App Data Directory: C:\Users\EC00427\.gemini\antigravity
Conversation ID: 20a4a426-14d3-4d16-975d-ab2e6dbdfe09
</user_information><skills>
Available skills:
- Architect Handoff Generation Protocol (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\architect_handoff_protocol\SKILL.md): Instrucciones estrictas para la creación de documentos de Handoff técnicos destinados a Agentes Especialistas (Backend, Frontend, QA) para evitar abstracciones y fallos sistémicos.
- Architecture Forensic Audit & Purge (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\architecture_forensic_audit\SKILL.md): Workflow completo de auditoría forense, detección de contradicciones cruzadas y saneamiento documental del ecosistema arquitectónico. Diseñado para ejecutarse periódicamente o ante cambios estructurales significativos.
- Code vs Architecture Compliance Audit (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\code_vs_architecture_compliance\SKILL.md): Auditoría de cumplimiento que verifica si el código construido (Backend Java + Frontend Vue 3) respeta las definiciones arquitectónicas prescritas en los ADRs, el Implementation Plan y el modelo C4. Orientado a detectar violaciones estructurales, no bugs funcionales.
- Grep Search Mitigation Policy (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\grep_search_governance\SKILL.md): Protocolo universal para mitigar errores de 'context canceled' y timeouts masivos al usar la herramienta de búsqueda en código.
- Handoff Quality Standard (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\handoff_quality_standard\SKILL.md): Estándar de calidad obligatorio para la redacción de documentos de Handoff inter-agente.
Garantiza consistencia estructural, trazabilidad, y eliminación de ambigüedad en toda
delegación técnica dentro del Enjambre de IA. Complementa el skill
`architect_handoff_protocol` (contenido arquitectónico) con un enfoque en FORMATO y CALIDAD.

- Protocolo de Despliegue de Ambiente E2E y Sembrado de Datos (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\e2e_setup_seeding_protocol\SKILL.md): Protocolo detallado para realizar la compilación, despliegue del entorno docker compose e2e, inyección quirúrgica de datos semilla UAT y solución de fallos comunes en WSL 2.
- SKILL (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\clean_code_standards\SKILL.md): Normativas estrictas de Clean Code. Obligatorio para cualquier escritura o refactorización de código.
- Zero-Mock & Real Database Enforcement Protocol (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\zero_mock_enforcement\SKILL.md): Regla de gobernanza estricta para erradicar el uso de mockAdapter.ts y obligar a pruebas E2E reales contra la base de datos Dockerizada, garantizando que Frontend, Backend y QA colaboren sobre un Full-Stack genuino.
- addyosmani_code_review (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\addyosmani_code_review\SKILL.md): Enforces senior-level code review discipline. Instructs the agent to self-audit all code changes against quality and architectural rules before proposing a commit.
- addyosmani_planning (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\addyosmani_planning\SKILL.md): Enforces senior-level planning discipline. Instructs the agent to create a detailed implementation plan before writing any code.
- addyosmani_sre_discipline (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\addyosmani_sre_discipline\SKILL.md): Enforces strict SRE discipline, Zero-Mock database policies, and Testing Pyramid compliance before confirming success.
- addyosmani_systematic_debugging (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\addyosmani_systematic_debugging\SKILL.md): Enforces systematic debugging, log analysis, and root cause isolation over blind code fixes.
- alphafold-database-fetch-and-analyze (C:\Users\EC00427\.gemini\config\plugins\science\skills\alphafold_database_fetch_and_analyze\SKILL.md): Retrieve and analyze AlphaFold predicted structures for a protein. Use when the user provides a specific UniProt Accession ID and wants structural confidence metrics (pLDDT), domain boundary analysis, or disorder assessment. Do not use if the user only has a protein name, gene name, or amino acid sequence — ask for a UniProt ID first.
- alphagenome-single-variant-analysis (C:\Users\EC00427\.gemini\config\plugins\science\skills\alphagenome_single_variant_analysis\SKILL.md): Analyzes genetic variant effects on gene expression (RNA-seq), chromatin accessibility (DNASE), histone marks (ChIP), and transcription factors using the AlphaGenome API. Use when the user asks about non-coding variant effects, pathogenicity, clinical significance, disease associations, functional effects, gene expression changes, splicing disruption, or regulatory effects in promoters and enhancers. Also use for resolving biological terms to tissue/cell-type ontologies (UBERON/CL) or analyzing variants in chr:pos:ref>alt format.
- android-cli (C:\Users\EC00427\.gemini\config\plugins\android-cli-plugin\skills\SKILL.md): Orchestrates Android development tasks including project creation, deployment, SDK management, and environment diagnostics using the `android` command-line tool.
- backend_sre_compilation_audit (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\backend_sre_compilation_audit\SKILL.md): Skill obligatoria para el Agente Backend. Exige auto-compilación con Maven y auditoría de arranque del puerto 8080 antes de entregar cualquier tarea o realizar un handoff.
- chembl-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\chembl_database\SKILL.md): Query the ChEMBL database for bioactive molecules, drug targets, bioactivity data, approved drugs, and chemical structures. Use when the user asks about compounds, targets, IC50/Ki values, drug mechanisms, or structure searches.
- clinical-trials-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\clinical_trials_database\SKILL.md): Query ClinicalTrials.gov via APIv2. Use when you want to search for trials by condition, drug, location, status, or phase; retrieve trial details by NCT ID; check eligibility/inclusion criteria; count trials across conditions or time periods; identify a sponsor's trial portfolio; find recruiting trials for patient matching.
- clinvar-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\clinvar_database\SKILL.md): Use when needing clinical significance, pathogenicity classifications (e.g., Pathogenic, Benign, VUS), clinical evidence rationales, or finding "hard positive" benchmark controls for human genomic variants.
- dbsnp-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\dbsnp_database\SKILL.md): Use when you want to look up, map, and search for short genetic variants (SNPs, indels) in NCBI's dbSNP database. Resolves between rsIDs, genomic coordinates in VCF format, and HGVS strings. For an rsID, returns variant type, gene associations, clinical significance, allele frequencies, and genomic coordinates (GRCh38).
- embl-ebi-ols (C:\Users\EC00427\.gemini\config\plugins\science\skills\embl_ebi_ols\SKILL.md): Query and search the EMBL-EBI Ontology Lookup Service (OLS) for biomedical ontology terms, definitions, and hierarchies across 250+ ontologies (e.g., GO, DOID, HP). Use when the user asks to search for terms, retrieve details, navigate hierarchies (parents, children, ancestors), look up properties and individuals, get autocomplete suggestions, or access ontology metadata and statistics.
- encode-ccres-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\encode_ccres_database\SKILL.md): Query the ENCODE Registry of cis-Regulatory Elements (cCREs) via the SCREEN GraphQL API, or make custom queries to the ENCODE Portal REST API for experiments and files (ChIP-seq peaks, etc.). Use when you want to query regulatory annotations or raw experimental data across human cell types.
- ensembl-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\ensembl_database\SKILL.md): Query the Ensembl database to resolve gene, transcript, and protein IDs, fetch genomic or protein sequences, retrieve gene structures (exons), and get variant consequence and effect predictions (VEP). Use this skill as a primary ID translator, genomic sequence database and variant effect prediction tool.
- foldseek-structural-search (C:\Users\EC00427\.gemini\config\plugins\science\skills\foldseek_structural_search\SKILL.md): Performs 3D structural searches of proteins against various databases (PDB, AlphaFold, CATH, MGnify, etc.) using the Foldseek API. Use ONLY when the user provides a physical 3D coordinate file (.cif, .mmcif, or .pdb) and wants to find structurally similar proteins. Do NOT use if the user only provides a protein sequence, gene name, or UniProt ID.
- frontend_build_audit (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\frontend_build_audit\SKILL.md): Skill obligatoria para el Agente Frontend. Exige auto-compilación con npm/Vite y auditoría de build exitoso antes de entregar cualquier tarea o realizar un handoff.
- gnomad-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\gnomad_database\SKILL.md): Query the Genome Aggregation Database (gnomAD). Use when determining the rarity or allele frequency of specific genetic variants, retrieving gene constraint metrics (pLI, LOEUF) to assess loss-of-function intolerance, finding variants in a genomic region or gene, or querying structural variants. Don't use for analyzing individual patient genomes, tracking somatic mutations in cancer (use COSMIC), or requesting raw sequencing reads (use ENA).
- gtex-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\gtex_database\SKILL.md): Use when you want to retrieve quantitative RNA expression data and variant eQTL information from the GTEx (Genotype-Tissue Expression) Project across 54 non-diseased tissue sites.
- human-protein-atlas-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\human_protein_atlas_database\SKILL.md): Use when you want to retrieve semi-quantitative protein expression and spatial localisation data from the Human Protein Atlas (HPA).
- hybrid_search_governance (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\hybrid_search_governance\SKILL.md): Skill obligatoria para todos los agentes (Arquitecto, Backend, Frontend, QA). Operacionaliza la LEY GLOBAL 0 (RAG-First Deep Context) definiendo un estricto protocolo  de "Cuádruple Check" (Conocimiento, Semántica, Estructura, Gobernanza) para blindar la  ventana de contexto, prevenir alucinaciones arquitectónicas y anclar el código al SSOT.

- interpro-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\interpro_database\SKILL.md): Identify domains, families, and sites in proteins; find all proteins in a family or sharing a domain; explore species distribution for a domain; annotate genomes with protein families and GO terms. InterPro combines 14 databases (e.g., Pfam, CDD) into one searchable resource. InterPro-N significantly expands annotation and sequence coverage with deep learning. Includes domain architecture (IDA) search.
- jaspar-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\jaspar_database\SKILL.md): Query the JASPAR database for Transcription Factor (TF) binding profiles. Use when retrieving Position Frequency Matrices (PFMs) or Position Weight Matrices (PWMs) for specific TFs, resolving gene symbols to JASPAR Matrix IDs, or getting TF metadata. Supports multiple output formats (MEME, TRANSFAC, PFM, JASPAR, YAML).
- literature-search-arxiv (C:\Users\EC00427\.gemini\config\plugins\science\skills\literature_search_arxiv\SKILL.md): Search for scientific papers, preprints, and publications on arXiv. Extract metadata, abstracts, and download full-text PDFs or HTML versions of papers. Use when the user asks to find research papers, literature, or specific arXiv IDs.
- literature-search-biorxiv (C:\Users\EC00427\.gemini\config\plugins\science\skills\literature_search_biorxiv\SKILL.md): Browse, filter, and download life sciences, biology, and medical preprints from bioRxiv and medRxiv. Supports fetching paper metadata by DOI, and browsing by date range with category and keyword filters. Keyword filtering is local, so date ranges MUST be narrow (1-4 weeks) with a category to prevent timeouts.
- literature-search-europepmc (C:\Users\EC00427\.gemini\config\plugins\science\skills\literature_search_europepmc\SKILL.md): Search Europe PMC for scientific literature and download open-access full texts and PDFs. Retrieve full-text XML/plain text by PMCID, get citation lists and bibliography.
- literature-search-openalex (C:\Users\EC00427\.gemini\config\plugins\science\skills\literature_search_openalex\SKILL.md): Query the OpenAlex scholarly database for research papers, authors, institutions, topics, sources, publishers, funders, geo-locations, and keywords. Use when searching academic papers, resolving DOIs, downloading open-access PDFs, finding an author's publications, aggregating bibliometric data (citation counts, h-index, impact factor), exploring the research taxonomies, or performing DOI lookups.
- ncbi-sequence-fetch (C:\Users\EC00427\.gemini\config\plugins\science\skills\ncbi_sequence_fetch\SKILL.md): Retrieve protein and nucleotide sequences from NCBI databases using E-utilities. Supports direct accession lookup, CDS translation, gene+organism search, locus lookup, PubMed-linked sequences, patent protein extraction, and organism+length fallback search. Use when you need to fetch biological sequences by accession, gene name, locus tag, PubMed ID, or patent number.
- openfda-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\openfda_database\SKILL.md): Query, search, and download data from the openFDA API for drugs, devices, foods, tobacco, cosmetics, animal and veterinary products, substances, and transparency data. Use for FDA adverse events, recalls, labeling, approvals, shortages, 510(k) clearances, NDC lookups, and any FDA safety or regulatory data query across all 28 API endpoints.
- opentargets-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\opentargets_database\SKILL.md): Query Open Targets Platform for target-disease associations, drug target discovery, tractability/safety data, genetics/omics evidence, known drugs, for therapeutic target identification.
- pdb-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\pdb_database\SKILL.md): Use when you want to search for or download experimentally-determined 3D structures for biomolecules (proteins, nucleic acids, bound ligands). Supports searching by sequence similarity, structure similarity, chemical and other attributes. Also use to get metadata about biomolecular structure experiments.
- po_ssot_gatekeeper (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\po_ssot_gatekeeper\SKILL.md): Habilidad exclusiva del Agente Product Owner para validar, mantener y proteger el alcance de negocio frente a desviaciones técnicas.
- protein-sequence-msa (C:\Users\EC00427\.gemini\config\plugins\science\skills\protein_sequence_msa\SKILL.md): Performs multiple sequence alignment of proteins with EBI Clustal Omega. Use when you need to align multiple sequences to assess similarity, domain conservation, or key residue conservation. Supports up to 4000 sequences and a maximum file size of 4 MB. Do not use to search for homologous proteins in a database (use MMseqs2, BLAST), align non-protein sequences (DNA, RNA), perform structural alignment (use Foldseek, PyMOL), or if you only have a single sequence.
- protein-sequence-similarity-search (C:\Users\EC00427\.gemini\config\plugins\science\skills\protein_sequence_similarity_search\SKILL.md): Searches for homologous protein sequences using MMseqs2 (fast, default) or BLAST (comprehensive, fallback). Trigger this whenever the user provides a protein sequence or FASTA file and asks to find homologues, sequence matches, or wants to infer protein function based on sequence similarity, but not when the user wants to infer protein function based on structural similarity.
- pubchem-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\pubchem_database\SKILL.md): Query PubChem, search by name/CID/SMILES, retrieve properties, similarity/substructure searches, bioactivity, for cheminformatics. Use when a user asks about a specific chemical, drug, or molecule.
- pubmed-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\pubmed_database\SKILL.md): Search PubMed for scientific literature, including published clinical trials. Fetch abstracts and full text. Link published research to biological databases (gene, protein, nucleotide, PubChem) to discover associations between papers and specific compounds or genes. Verify medical spelling, match raw citations, and cache result sets for bulk processing. Interfaces NCBI E-utilities and PMC BioC APIs.
- pymol (C:\Users\EC00427\.gemini\config\plugins\science\skills\pymol\SKILL.md): Visualize, analyze, and render protein and molecular structures using PyMOL. Use when the user wants to create images of protein structures, perform structural alignments or superposition, measure distances or contacts, highlight binding sites or active site residues, color by B-factor/pLDDT, or analyze protein-ligand interactions. Do not use for docking, molecular dynamics, or sequence-only analysis.
- qa_e2e_validation_audit (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\qa_e2e_validation_audit\SKILL.md): Skill obligatoria para el Agente QA/DevOps. Exige ejecución empírica de TODA LA PIRÁMIDE DE PRUEBAS  (Unitarias, Integración, E2E) usando JUnit, Vitest y Playwright. Incluye la adjunción de evidencia  verificable (screenshots/video/logs) y prohibición absoluta de reportar "pass" sin pruebas de supervivencia.

- quickgo-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\quickgo_database\SKILL.md): Query the QuickGO and Evidence & Conclusion Ontology (ECO) REST API. Use this when you need to map genes to biological processes, molecular functions, or cellular components, find genes associated with a specific pathway/GO term, or explore the Gene Ontology hierarchy. Do not use for querying drug targets (use OpenTargets) or mechanistic signaling pathway diagrams (use KEGG).
- reactome-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\reactome_database\SKILL.md): Query the Reactome database (Analysis and Content Services). Use when the user asks about pathway analysis, gene list enrichment, retrieving results by token, finding unmapped or not-found identifiers, mapping identifiers, reaction participants (inputs, outputs), pathway hierarchy (including top-level pathways), diagram export, cross-reference mapping, or searching the knowledgebase.
- science-skills-common (C:\Users\EC00427\.gemini\config\plugins\science\skills\science_skills_common\SKILL.md): Shared Python package for Science Skills, currently containing http_client -- a unified HTTP client with rate limiting, retries, and exponential backoff. Not a standalone agent skill. Do not invoke directly.
- scienceskillscommon (C:\Users\EC00427\.gemini\config\plugins\science\skills\scienceskillscommon\SKILL.md): Shared Python package for Science Skills, currently containing http_client -- a unified HTTP client with rate limiting, retries, and exponential backoff. Not a standalone agent skill. Do not invoke directly.
- string-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\string_database\SKILL.md): Query the STRING database for protein-protein interactions (PPIs), functional enrichment, and homology. Use when the user asks about interactions between specific proteins, interaction evidence, confidence scores, protein interaction partners, or pathway enrichments.
- ucsc-conservation-and-tfbs (C:\Users\EC00427\.gemini\config\plugins\science\skills\ucsc_conservation_and_tfbs\SKILL.md): Fetch Evolutionary Conservation scores (phyloP, phastCons) and Transcription Factor Binding Sites (TFBS) from the UCSC Genome Browser. Use when analyzing whether genomic variants or regions are evolutionarily conserved, functionally important, or bounded by TF regulators across major projects (ENCODE, JASPAR, ReMap).
- unibind-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\unibind_database\SKILL.md): Queries the UniBind database for experimentally validated transcription factor (TF) binding sites. Use when retrieving direct TF-DNA interaction datasets, downloading binding site coordinates (BED/FASTA) for local analysis, or listing available datasets by species, cell line, or TF name. Don't use to query specific intervals, locations, genes, motif models or expression data.
- uniprot-database (C:\Users\EC00427\.gemini\config\plugins\science\skills\uniprot_database\SKILL.md): Access protein metadata, function, taxonomy, and sequences across UniProtKB, UniParc, and UniRef. Use when searching for proteins, mapping identifiers, or retrieving functional annotations and publications. Don't use for sequence alignment, protein folding, or sequence similarity search (use specialized skills for those tasks).
- uv (C:\Users\EC00427\.gemini\config\plugins\science\skills\uv\SKILL.md): Checks whether the uv Python package manager is installed and installs it if missing. Ensures uv is on PATH. Use when another skill requires uv as a prerequisite.
- workflow-skill-creator (C:\Users\EC00427\.gemini\config\plugins\science\skills\workflow_skill_creator\SKILL.md): Distills a completed user workflow or interaction into a reusable agent skill. Use when the user asks to turn their workflow, interaction, or multi-step process into a skill, or when they say "make this a skill", "create a skill from what we just did", "package this workflow" or similar. Do not use for creating skills from scratch without an existing workflow (use a generic skill-creator for that).
- yudhi_architecture_compliance (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\yudhi_architecture_compliance\SKILL.md): Enforces Hexagonal Architecture, DDD, and Camunda isolation. Integrates the code auditing checklists (R1-R8, F1-F7) to detect structural violations.
- yudhi_clean_code_standards (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\yudhi_clean_code_standards\SKILL.md): Enforces Yudhi Armyndharis' clean code standards for Java 17 and Vue 3 / TypeScript codebases.
- yudhi_database_migrations (z:\home\haroltandrsgmezagu\proyectos\ibpms-platform\.agents\skills\yudhi_database_migrations\SKILL.md): Enforces database migration rules and PostgreSQL query optimizations (indexes, pgvector, JSONB).

</skills><subagent_reminder>
You are running as a subagent, invoked by a caller agent (name: "main agent", id: "02649556-e4f9-45c9-ba0c-fc5d205be1d9"). You MUST use send_message to communicate all results, reports, and updates back to the caller. Your response is NOT automatically relayed — if you do not call send_message, the caller will only know that you have gone idle. Always use the caller's id as the Recipient and "main agent" as the RecipientName.

Text you generate outside of send_message will NOT be seen by the caller, so keep them brief. Put all important information — findings, summaries, conclusions — into your send_message calls instead. You can also share files by including their absolute paths in your message; the caller can then read them directly.
</subagent_reminder><subagents>
Available subagents:
- self: Subagent that inherits the parent agent's full configuration including tools, system prompt, and model. Use this when you need to run a task in a separate conversation context but with the same capabilities as the current agent.
- teamwork_preview_orchestrator: Pure orchestrator. Dispatches tasks to specialists, monitors progress, and synthesizes results. Does not write code directly.
- teamwork_preview_victory_auditor: Independent post-victory auditor. Spawned by the Sentinel when the team claims project completion. Conducts a 3-phase audit (timeline, cheating detection, independent test execution) with zero shared context from the implementation swarm. Reports a structured verdict.

</subagents><USER_REQUEST>
Implement process version tag auto-suggestion homologated to v0 (0.0.0) for new draft processes in the BPMN Modeler (US-005).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Modeler Version Tag Suggestion
In BpmnDesigner.vue, modify the auto-suggestion logic so that when a process is a draft (currentVersion is 0) or lacks a version tag, the version tag is suggested as '0.0.0' instead of '1.0.0'.

### R2. Unit Test Alignment
In BpmnDesigner.spec.ts, update the test case "Debe auto-sugerir '1.0.0' para el Version Tag..." to "Debe auto-sugerir '0.0.0' para el Version Tag...", asserting that processVersionTag and the businessObject's versionTag attribute are set to '0.0.0'.

### R3. Compliance with ADR-001, Yudhi Clean Code, and SRE Discipline
- Ensure strict typing (no any at all costs in newly modified files).
- Keep composition API setup below 150 lines or modularized.
- Enforce Zero-Mock principles: run and verify that frontend tests pass in WSL.
- Compile and build successfully with npm run build.

## Acceptance Criteria

### Modeler UI
- [ ] Version tag auto-suggests "0.0.0" when creating or loading a new process (v0).
- [ ] Vitest unit tests in BpmnDesigner.spec.ts pass successfully in WSL.
- [ ] Frontend production build compiles cleanly without errors.
</USER_REQUEST>
<ADDITIONAL_METADATA>
The current local time is: 2026-06-10T14:58:54-05:00.
</ADDITIONAL_METADATA>

## 2026-06-11T01:05:22Z

<user_information>
The USER's OS version is windows.
The user has 1 active workspaces, each defined by a URI and a CorpusName. Multiple URIs potentially map to the same CorpusName. The mapping is shown as follows in the format [URI] -> [CorpusName]:
z:\home\haroltandrsgmezagu\proyectos\ibpms-platform -> haroldklag85/ibpms-platform
Code relating to the user's requests should be written in the locations listed above. Avoid writing project code files to tmp, in the .gemini dir, or directly to the Desktop and similar folders unless explicitly asked.
App Data Directory: C:\Users\EC00427\.gemini\antigravity
Conversation ID: a118fec6-8848-496d-aa3e-d8e9220b22b7
</user_information>

<USER_REQUEST>
Resolve the BPMN Modeler (US-005) bugs (HTTP 400 Bad Request on deploy request, HTTP 409 Conflict on lock heartbeat, and E2E test timeout failure due to 404 redirection).

Working directory: z:/home/haroltandrsgmezagu/proyectos/ibpms-platform
Integrity mode: development

## Requirements

### R1. Disable ID Técnico modification after draft creation
In `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, disable the `ID Técnico` input field once a process has been saved or exists as a draft. Specifically, ensure that `isNewProcess.value` is set to `false` after a successful call to `saveDraft` in the UI, and bind the input field's `:disabled` attribute to `!isNewProcess`.

### R2. Automatic Lock Acquisition upon Process Load
In `frontend/src/views/admin/Modeler/BpmnDesigner.vue`, implement automatic lock acquisition when a process is loaded or created (after verifying that it is not locked by another user). This will send a `POST /design/processes/{id}/lock` request to the backend so the database has an active lock, avoiding `409 (Conflict)` errors during subsequent heartbeat calls.

### R3. E2E Test activeRole Bypass via atob Interceptor
In `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts`, modify the `beforeEach` hook to override the global `window.atob` function. The overridden function must intercept the decoding of the JWT payload, shifting the role `"ibpms_rol_SUPER_ADMIN"` to the first element in the `roles` array. This ensures Pinia's `authStore` initializes `activeRole` as `"ROLE_SUPER_ADMIN"`, bypassing the navigation guard's fake 404 page and allowing Playwright to locate and interact with the modeler.

## Acceptance Criteria

### UI Modeler Behavior
- [ ] The `ID Técnico` input field is disabled when viewing or editing an existing saved process.
- [ ] Opening or creating a process automatically initiates a lock request (`POST /lock`), which registers the lock in the backend.
- [ ] No `409 (Conflict)` errors are thrown in the console by the heartbeat timer.

### E2E Certification
- [ ] Playwright E2E test `us005-bpmn-modeler-persistence.e2e.spec.ts` passes 100% in WSL.
- [ ] Frontend production build compiles successfully with `npm run build`.

## Test-First Strategy (Mandatory)
The agent team must strictly follow this execution order:
1. First, implement the changes to `frontend/e2e/certification/us005-bpmn-modeler-persistence.e2e.spec.ts` (the `window.atob` interceptor).
2. Run the E2E test suite using WSL to confirm that the test fails (or fails on the newly expected behaviors like auto-locking, rather than timing out on 404).
3. Once the test failure is validated and documented, implement the frontend changes in `BpmnDesigner.vue` (R1 and R2).
4. Run the E2E test suite again to confirm all tests pass successfully (green light).

## Required Subagent Skills & Standards
Every specialist agent assigned to this task must apply and follow these skills:
- `addyosmani_sre_discipline` (for Zero-Mock database enforcement and validation)
- `addyosmani_planning` (for systematic task planning)
- `addyosmani_code_review` (for pre-commit validation)
- `yudhi_architecture_compliance` (for structural hexagonal compliance)
- `yudhi_database_migrations` (for DB mapping verification)
- `handoff_quality_standard` (for quality documentation handoffs)
</USER_REQUEST>

