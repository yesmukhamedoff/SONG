# Docker for Song


## Configuration
Since docker-compose will be running on the host network, the ports specified in the `docker-compose.yml` file **MUST** be free before bringing up the environment. To check on Mac/Linux, simply run:

```bash
nmap -sT localhost
```

The command will output a list of used ports on the host network. 
If a port is being used by the local machine, some of the docker services might not be able to run. There are 2 ways to fix this:
1. Find the process using the port, and kill it. On Mac/Linux, you can find the process by:
`lsof -i :<port> | grep LISTEN`
2. Change the port name of the service. This can be done by modifying the port number in the `docker-compose.yml`

## Build and Start services
To build the images, and then start the services, run:

```bash
docker-compose build
docker-compose up
```

## Execution
The song file upload and registration process is composed of 4 stages, and the following will explain all of them. For the sake of consistency, all paths and commands are relative to  

### Stage 1: Verify Connectivity
Before using the client, the connection must be verified using the command below. The response is a boolean: if it returns `true` then the song-server is running, otherwise it is not. If this step fails, then the configuration file for the client `./data/client/conf/application.properties` must be updated with the correct url of the song-server.

```bash
./sing status -p
```

### Stage 2: Uploading the Payload
1. Create an upload payload using the schema defined in
[sequencingRead.json](https://github.com/icgc-dcc/SONG/blob/develop/song-server/src/main/resources/schemas/sequencingRead.json)
and
[variantCall.json](https://github.com/icgc-dcc/SONG/blob/develop/song-server/src/main/resources/schemas/variantCall.json)
Here is an [exampleSequencingRead.json](https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/sequencingRead.json).
and 
[exampleVariantCall.json](https://github.com/icgc-dcc/SONG/blob/develop/src/test/resources/fixtures/variantCall.json) upload payload.
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

### Stage 3: Generating the Manifest
### Stage 4: Uploading the Files
### Stage 5: Publishing the Analysis




This example will go through full file upload and registration process. Once the services are running, all generated data will reside in the `./data` directory. To begin, change directories:
```bash
cd data
```

Check that the song-server


# Check the song-server is running and that the client is properly configured
./sing status -p

# Upload the payload
./sing upload -f ../example/exampleVariantCall.json





To see if SONG is running, open another terminal window, go to this directory, and run:
```
../singTest
```

All tests labelled **[ERROR TEST]** should fail; the rest should pass. 
