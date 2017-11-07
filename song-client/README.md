## <span style="color: #1ab2ff">INITIAL SETUP</span>

1. Inorder to interact with song-server, and upload/download from the dcc-storage-server, an oauth2 access token with `song.upload` and `aws.upload`/`collab.upload` is required. Although `aws.download`/`collab.download` scope is not required, it is good to have in addition to the required scopes, so files can be downloaded. If client will be using the song-server in ICGC, appropriate [DACO Cloud Access](http://docs.icgc.org/cloud/guide/#daco-cloud-access) and [Access Tokens](http://docs.icgc.org/cloud/guide/#access-tokens) are required. Refer to the [ICGC Authorization](http://docs.icgc.org/cloud/guide/#authorization) instructions. 

    Additionally, the scope `song.upload` allows access to **all** studyIds. If access to a specific studyId is needed, the scope `song.<studyId>.upload` can be used.

2. Download the LATEST song-client from 
[https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/[RELEASE]/song-client-[RELEASE]-dist.tar.gz](https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/[RELEASE]/song-client-[RELEASE]-dist.tar.gz)

3. Untar it, set SING_HOME env variable. The following summarizes all of this:
```bash
 wget https://artifacts.oicr.on.ca/artifactory/dcc-release/org/icgc/dcc/song-client/[RELEASE]/song-client-[RELEASE]-dist.tar.gz
 tar zxvf song-client-*.tar.gz
 mv song-client-* song-client
 export SING_HOME="$PWD/song-client"
```

4. Update the `$SING_HOME/conf/application.yml` file with the following
```yaml
serverUrl: http://142.1.177.84:8080/
debug: false
accessToken: <your access token which has song.upload scope>
```

5. Use the song-client to check if the song-server is running
```bash
$SING_HOME/bin/sing status -p
```

## <span style="color: #1ab2ff">EXECUTION</span>
### <span style="color: #e68a00">Stage 1: SONG Upload</span>
1. Create an upload payload using the schema defined in 
[sequencingRead.json](https://github.com/icgc-dcc/SONG/blob/develop/song-server/src/main/resources/schemas/sequencingRead.json)
and
[variantCall.json](https://github.com/icgc-dcc/SONG/blob/develop/song-server/src/main/resources/schemas/variantCall.json)
Here is an [exampleSequencingRead.json](https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/sequencingRead.json).
and and [exampleVariantCall.json](https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/variantCall.json) upload payload.
You can also download them, and then modify them:
```bash
wget https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/sequencingRead.json
wget https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/variantCall.json
```
    __NOTE:__  Ensure the `fileName` field in the `file` object is an __ABSOLUTE__ path. If is not, then the ICGC-DCC-Storage client (in [Stage 3](#stage-3-icgc-storage-upload)) will assume it is in its working directory.
Refer to the [Metadata Upload Rules](https://github.com/icgc-dcc/SONG/blob/develop/METADATA_RULES.md) which have additional info

2. Upload the payload file to the server
```bash
$SING_HOME/bin/sing upload -f <path_to_payload_json>
```
    The response will be the uploadId and is needed for the following steps

3. Check the status of the upload, using the uploadId
```bash
$SING_HOME/bin/sing status -u <uploadId>
```
    Ensure the response has the state `VALIDATED`

### <span style="color: #e68a00">Stage 2: SONG Manifest Generation</span>
1. Save the metadata
```bash
$SING_HOME/bin/sing save -u <uploadId>
```
    The response will be the analysisId

2. Generate a manifest for the `icgc-dcc-storage` client in [Stage 3](#stage-3-icgc-storage-upload)
```bash
$SING_HOME/bin/sing manifest -a <analysisId> -f /tmp/my_manifest.txt
```

### <span style="color: #e68a00">Stage 3: ICGC-STORAGE Upload</span>

Upload the manifest file to the `icgc-dcc-storage` using the [icgc-storage-client](http://docs.icgc.org/software/binaries/#storage-client)
```bash
$ICGC_STORAGE_CLIENT_HOME/bin/icgc-storage-client upload --manifest /tmp/my_manifest.txt
```

### <span style="color: #e68a00">Stage 4: SONG Publish</span>

Using the same analysisId as before, publish it. Essentially, this is the handshake between the metadata stored in the SONG server (via the analysisIds) and the files stored in the icgc-dcc-storage server (the files described by the analysisId)
```java
$SING_HOME/bin/sing publish -a <analysisId>
```
The response should be "AnalysisId <analysisId> successfully published"

