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
![Screenshot from 2023-08-23 01-57-11](https://github.com/EBIvariation/eva-seqcol/assets/82417779/798c9b49-81bb-438e-90d6-6bc5ee532331)

## Data Model
After multiple evaluations of different data models, we agreed to use the following model :
![Screenshot from 2023-08-23 00-56-56](https://github.com/EBIvariation/eva-seqcol/assets/82417779/0b2c002b-a497-47ee-a3cc-0395de97ca4f)

## Endpoints
Note: the seqCol service is currently deployed on server **45.88.81.158**, under port **8081**
### Exposed endpoints
- `PUT - SERVER_IP:PORT/eva/webservices/seqcol/admin/seqcols/{asm_accession}`
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/collection/{seqCol_digest}?level={level}`
- `GET - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}/{seqColB_digest}`
- `POST - SERVER_IP:PORT/eva/webservices/seqcol/comparison/{seqColA_digest}; body = {level 2 JSON representation of another seqCol}`
### Usage and description
For a detailed, user friendly documentation of the API's endpoints, please visit the seqCol's [swagger page](#todo)
## Compile
This web service has some authenticated endpoints. The current approach to secure them is to provide the credentials in the src/main/resources/application.properties file at compilation time, using maven profiles.

The application also requires to be connected to an external database (PostgreSQL by default) to function. The credentials for this database need to be provided at compilation time using the same maven profiles.

You can edit the maven profiles values in **pom.xml** by locating the below section and changing the values manually or by setting environemnt variables. Alternatively, you can make the changes directly on the **application.properties** file.

Use `<ftp.proxy.host>` and `<ftp.proxy.port>` to configure proxy settings for accessing FTP servers (such as NCBI's). Set them to `null` and `0` to prevent overriding default the proxy configuration.

Set a boolean flag using `<contig-alias.scaffolds-enabled>` to enable or disable parsing and storing of scaffolds in the database.
```
 <profiles>
	<profile>
		<id>seqcol</id>
		<properties>
			<spring.profiles.active>seqcol</spring.profiles.active>
			<seqcol.db-url>jdbc:postgresql://${env.SERVER_IP}:${env.POSTGRES_PORT}/seqcol_db</seqcol.db-url>
			<seqcol.db-username>${env.POSTGRES_USER}</seqcol.db-username>
			<seqcol.db-password>${env.POSTGRES_PASS}</seqcol.db-password>
			<seqcol.ddl-behaviour>${env.DDL_BEHAVIOUR}</seqcol.ddl-behaviour>
			<seqcol.admin-user>${env.ADMIN_USER}</seqcol.admin-user>
			<seqcol.admin-password>${env.ADMIN_PASSWORD}</seqcol.admin-password>
			<ftp.proxy.host>${optional default=null}</ftp.proxy.host>
			<ftp.proxy.port>${optional default=0}</ftp.proxy.port>
			<contig-alias.scaffolds-enabled>${optional default=false}</contig-alias.scaffolds-enabled>
		</properties>
		<activation>
			<activeByDefault>true</activeByDefault>
		</activation>
	</profile>
 </profiles>
```
Once that's done, you can trigger the variable replacement with the `-P` option in maven. Example: `mvn clean install -Pseqcol` to compile the service including tests or `mvn clean install -Pseqcol -DskipTests` to ignore tests.

You can then run: ` mvn spring-boot:run` to run the service.

## Technologies used
- Spring Boot v2.7.13
- PostgreSQL Database v15.2
- Swagger v3 (springdoc-openapi implementation)

## Useful Links
- [seqCol](https://seqcol.readthedocs.io/en/dev/), [seqcol-spec](https://github.com/ga4gh/seqcol-spec/blob/master/docs/decision_record.md), [specification](https://github.com/ga4gh/seqcol-spec/blob/6e28693ce043ae993b9a67820cc9507f444884d0/docs/specification.md) (Specification's details and docs)
- [GA4GH refget API meetings](https://docs.google.com/document/d/18VIGjcEC7B8XMbqh1E2afTMdbEo9WMK1/edit) (Minutes for the refget API meetings)
- [Python implementation](https://github.com/refgenie/seqcol/tree/46675b669ae07db9da4fc3d113fefa2c1667b1fb/seqcol) (A python implementation of the sequence collection specification)
