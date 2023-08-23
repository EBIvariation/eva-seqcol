# eva-seqcol
This is a Java implementation of the **sequence collection** specification to represent INSDC assemblies.
To learn more about the sequence collection specification, please refer to [seqCol](https://seqcol.readthedocs.io/en/dev/), [seqcol-spec](https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md) and/or the [specification](https://github.com/ga4gh/seqcol-spec/blob/6e28693ce043ae993b9a67820cc9507f444884d0/docs/specification.md).

Briefly, the main issue that the seqcol-spec addresses is that genomes' central providers such as INSDC (e.g. NCBI, ENA), Ensembl or UCSC may agree on the sequence being used but they often differ on the **naming** of these sequences.

## Project Goals
The main goals of this API is to provide:
- A mechanism to **ingest** a sequence collection object into the database.
- A mechanism to **fetch/resolve** a sequence collection object given its level 0 digest.
- A mechanism to **compare** two sequence collection objects to understand their compatibility

## Important Workflows
### SeqCol Ingestion Workflow
![Screenshot from 2023-08-23 00-35-17](https://github.com/waterflow80/Gsoc-23/assets/82417779/88d0b699-15ee-4ac0-abb8-1c4765d84eda)

### SeqCol digest calculation process
![Screenshot from 2023-08-23 00-27-37](https://github.com/waterflow80/Gsoc-23/assets/82417779/c1fe421b-10af-42f3-8531-1607aa59e6fb)

## Data Model
After multiple evaluations of different data models, we agreed to use the following model :
![Screenshot from 2023-08-23 00-56-56](https://github.com/waterflow80/Gsoc-23/assets/82417779/b3cfc183-630c-4884-a5a7-35cb5d8163e2)
- **sequence_collections_l1:** stores the seqCol level 1 objects. Example:

  | digest (level 0) | naming_convention | seq_col_level1object |
    | ------------- | ------------- | ------------- |
  | rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE | UCSC | ```{"names": "rIeQU2I79mbAFQwiV_kf6E8OUIEWq5h9", "lengths": "Ms_ixPgQMJaM54dVntLWeovXSO7ljvZh", "sequences": "dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6", "md5-sequences": "_6iaYtcWw4TZaowlL7_64Wu9mbHpDUw4", "sorted-name-length-pairs": "qCETfSy_Ygmk0qtUJwI8V6SdsYX_AC53"}``` |
  | 3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq | GENBANK | ```{"names": "mfxUkK3J5y7BGVW7hJWcJ3erxuaMX6xm", "lengths": "Ms_ixPgQMJaM54dVntLWeovXSO7ljvZh",```**``` "sequences": "dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6```**```", "md5-sequences": "_6iaYtcWw4TZaowlL7_64Wu9mbHpDUw4", "sorted-name-length-pairs": "QFuKs5Hh8uQwwUtnRxIf8W3zeJoFOp8Z"}``` |
- **seqcol_extended_data:** stores the seqCol extended (exploded) level 2 seqCol data. Each entry has a digest that corresponds to one of the seqCol level 1 attribute's values. Example:

  | digest | extended_seq_col_data |
    | ------------- | ------------- |
  | **dda3Kzi1Wkm2A8I99WietU1R8J4PL-D6** | ```{"object": ["SQ.lZyxiD_ByprhOUzrR1o1bq0ezO_1gkrn", "SQ.vw8jTiV5SAPDH4TEIZhNGylzNsQM4NC9", "SQ.A_i2Id0FjBI-tQyU4ZaCEdxRzQheDevn", "SQ.QXSUMoZW_SSsCCN9_wc-xmubKQSOn3Qb", "SQ.UN_b-wij0EtsgFqQ2xNsbXs_GYQQIbeQ", "SQ.z-qJgWoacRBV77zcMgZN9E_utrdzmQsH", "SQ.9wkqGXgK6bvM0gcjBiTDk9tAaqOZojlR", "SQ.K8ln7Ygob_lcVjNh-C8kUydzZjRt3UDf", "SQ.hb1scjdCWL89PtAkR0AVH9-dNH5R0FsN", "SQ.DKiPmNQT_aUFndwpRiUbgkRj4DPHgGjd", "SQ.RwKcMXVadHZub1qL0Y5c1gmNU1_vHFme", "SQ.1sw7ZtgO9JRb1kUEuhVz1wBix5_8Opci", "SQ.V7DQqMKG7bcyxiMZK9wNjkK-udR7hrad", "SQ.R8nT1N2qQFMc_uVMQUVMw-D2GcVmb5v6", "SQ.DPa_ORXLkGyyCbW9SWeqePfortM-Vdlm", "SQ.koyLEKoDOQtGHjb4r0m3o2SXxI09Z_sI"]}``` |
  | _6iaYtcWw4TZaowlL7_64Wu9mbHpDUw4 | ... |
  | rIeQU2I79mbAFQwiV_kf6E8OUIEWq5h9 | ... |
  | Ms_ixPgQMJaM54dVntLWeovXSO7ljvZh | ... |
  | qCETfSy_Ygmk0qtUJwI8V6SdsYX_AC53 | ... |
  | mfxUkK3J5y7BGVW7hJWcJ3erxuaMX6xm | ... |
  | QFuKs5Hh8uQwwUtnRxIf8W3zeJoFOp8Z | ... |

The digests in **bold** indicates how the data is handled **recursively**, pretty much, between the two tables.
## Endpoints
Note: the seqCol service is currently deployed on server **45.88.81.158**, under port **8081**
### Exposed endpoints
- `PUT - SERVER_IP:PORT/eva/webservices/seqcol/admin/seqcols/{asm_accession}`
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/collection/{seqCol_digest}?level={level}`
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}/{seqColB_digest}`
- `POST - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}; body = {level 2 JSON representation of another seqCol}`
### Usage and description
- `PUT - SERVER_IP:PORT/eva/webservices/seqcol/admin/seqcols/{asm_accession}`
  -  Description: Ingest all possible (all possible naming conventions) seqCol objects in the database for the given assembly accession and return the list of digests of the inserted seqCol objects
  -  Permisssion: Authenticated endpoint (requires admin privileges)
  -  Example: PUT - [http://45.88.81.158:8081/eva/webservices/seqcol/admin/seqcols/GCA_000146045.2](http://45.88.81.158:8081/eva/webservices/seqcol/admin/seqcols/GCA_000146045.2)
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/collection/{seqCol_digest}?level={level}`
  -  Description: Retrieve the seqCol object that has the given seqCol_digest in the given level representation (level should be either 1 or 2, **default=1**)
  -  Permisssion: Public endpoint
  -  Example: GET - [http://45.88.81.158:8081/eva/webservices/seqcol/collection/3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq](http://45.88.81.158:8081/eva/webservices/seqcol/collection/3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq)
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}/{seqColB_digest}`
  -  Description: Compare two seqCol objects given their level 0 digests
  -  Permisssion: Public endpoint
  -  Example: GET - [http://45.88.81.158:8081/eva/webservices/seqcol/comparison/rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE/3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq](http://45.88.81.158:8081/eva/webservices/seqcol/comparison/rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE/3mTg0tAA3PS-R1TzelLVWJ2ilUzoWfVq)
- `POST - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}; body = {level 2 JSON representation of another seqCol}`
  -  Description: Compare two seqCol objects given the first's level 0 digest, and the other's level 2 JSON representation
  -  Permisssion: Public endpoint
  -  Example: POST - [http://45.88.81.158:8081/eva/webservices/seqcol/comparison/rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE](http://45.88.81.158:8081/eva/webservices/seqcol/comparison/rkTW1yZ0e22IN8K-0frqoGOMT8dynNyE), Body =
    ```
  {
      "sequences": [
        "SQ.lZyxiD_ByprhOUzrR1o1bq0ezO_1gkrn",
        "SQ.vw8jTiV5SAPDH4TEIZhNGylzNsQM4NC9",
        "SQ.A_i2Id0FjBI-tQyU4ZaCEdxRzQheDevn"
      ],
      "names": [
        "I",
        "II",
        "III"
      ],
      "lengths": [
        "230218",
        "813184",
        "316620"
      ]
  }
## Deployment
## Development
## Useful Links
- [seqCol](https://seqcol.readthedocs.io/en/dev/), [seqcol-spec](https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md), [specification](https://github.com/ga4gh/seqcol-spec/blob/6e28693ce043ae993b9a67820cc9507f444884d0/docs/specification.md) (Specification's details and docs)
- [GA4GH refget API meetings](https://docs.google.com/document/d/18VIGjcEC7B8XMbqh1E2afTMdbEo9WMK1/edi) (Minutes for the refget API meetings)
- [Python implementation](https://github.com/refgenie/seqcol/tree/46675b669ae07db9da4fc3d113fefa2c1667b1fb/seqcol) (A python implementation of the sequence collection specification)
## Acknowledgements
We should give credit to [Timothee Cezard](https://github.com/tcezard), [April Shen](https://github.com/apriltuesday) and [Sundar Venkataraman](https://github.com/sundarvenkata-EBI) for their remarkable efforts in mentoring and supporting this project. 
