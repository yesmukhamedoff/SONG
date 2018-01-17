import os
import re
import time
from functools import total_ordering

from enum import unique, Enum
import logging

from song import utils as utils
from song.client import UploadClient, Api, StudyClient
from song.utils import check_state, SongClientException
from song.model import ApiConfig, Study, SongError
import json

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("song.tools")


class EGAUploader(object):

    def __init__(self, server_url, access_token, payload_dir, debug=False):

        # Dependencies
        self.payload_dir = os.path.realpath(payload_dir)

        # Config
        self.server_url = server_url
        self.debug = debug
        self.access_token = access_token
        self.is_async_validation = True
        self.ignore_analysis_id_collisions = True

        # State
        self.__upload_status_map = {}
        self.__api = None

        # Check
        utils.check_dir(self.payload_dir)

    def __calc_depth(self, root_path ):
        rel_root_path = os.path.realpath(root_path).replace(self.payload_dir, '')
        return len(rel_root_path.split(os.sep)) - 1

    def __use_root_dir(self, root_dir):
        is_study_level = self.__calc_depth(root_dir) == 1
        study_id_candidate = root_dir.split(os.sep)[-1]
        is_not_hidden_dir = re.search('^\.', study_id_candidate) is None
        return is_not_hidden_dir and is_study_level, study_id_candidate

    def __build_api_config(self, study_id):
        return ApiConfig(self.server_url, study_id, self.access_token, debug=self.debug)

    def __build_api(self, study_id):
        return Api(self.__build_api_config(study_id))

    def __build_study_client(self, study_id):
        return StudyClient(self.__build_api(study_id))

    def __setup_study(self, study_id):
        study_client = self.__build_study_client(study_id)
        if not study_client.has(study_id):
            study = Study(studyId=study_id, name="N/A", organization="ICGC", description="None")
            study_client.create(study)

    def upload_all(self):
        for root, dirs, files in os.walk(self.payload_dir):
            use_root_dir, study_id = self.__use_root_dir(root)

            if use_root_dir:
                print ("study_id = {}    files= {} ".format(study_id, files))
                self.__setup_study(study_id)

                filtered_file_list = filter(lambda f: f.endswith('.json'), files)
                total_size = len(filtered_file_list)
                file_count = 0
                for file in filtered_file_list:
                    filename = root+os.sep+file
                    file_upload_obj = self.get_file(study_id, filename)
                    file_upload_obj.upload()
                    file_count += 1
                    print("Uploaded ( {} / {} ) [{}]:  {}".format(file_count, total_size, study_id, filename))


    def status_all(self):
        for root, dirs, files in os.walk(self.payload_dir):
            use_root_dir, study_id = self.__use_root_dir(root)
            if use_root_dir:
                filtered_file_list = filter(lambda f: f.endswith('.json'), files)
                total_size = len(filtered_file_list)
                file_count = 0
                # TODO: refactor this repitition out
                for file in filtered_file_list:
                    filename = root+os.sep+file
                    file_upload_obj = self.get_file(study_id, filename)
                    file_upload_obj.update_status()
                    file_count += 1
                    print("Status Checked ( {} / {} ) [{}]:  {}".format(file_count, total_size, study_id, filename))

    def save_all(self):
        for root, dirs, files in os.walk(self.payload_dir):
            use_root_dir, study_id = self.__use_root_dir(root)
            if use_root_dir:
                filtered_file_list = filter(lambda f: f.endswith('.json'), files)
                total_size = len(filtered_file_list)
                file_count = 0
                for file in filtered_file_list:
                    filename = root+os.sep+file
                    file_upload_obj = self.get_file(study_id, filename)
                    file_upload_obj.save()
                    file_count += 1
                    print("Saved ( {} / {} ) [{}]:  {}".format(file_count, total_size, study_id, filename))

    def publish_all(self):
        for root, dirs, files in os.walk(self.payload_dir):
            use_root_dir, study_id = self.__use_root_dir(root)
            if use_root_dir:
                filtered_file_list = filter(lambda f: f.endswith('.json'), files)
                total_size = len(filtered_file_list)
                file_count = 0
                for file in filtered_file_list:
                    filename = root+os.sep+file
                    file_upload_obj = self.get_file(study_id, filename)
                    file_upload_obj.publish()
                    file_count += 1
                    print("Published ( {} / {} ) [{}]:  {}".format(file_count, total_size, study_id, filename))

    def get_file(self, study_id, filename):
        if self.__upload_status_map.has_key(study_id):
            if self.__upload_status_map[study_id].has_key(filename):
                return self.__upload_status_map[study_id][filename]
        else:
            self.__upload_status_map[study_id] = {}

        api = self.__build_api(study_id)
        file_upload = FileUploadClient(
            api, filename,
            is_async_validation=self.is_async_validation,
            ignore_analysis_id_collisions=self.ignore_analysis_id_collisions)
        self.__upload_status_map[study_id][filename] = file_upload
        return file_upload

    def get_studies(self):
        return self.__upload_status_map.keys()

    def get_files(self, study_id):
        if self.__upload_status_map.has_key(study_id):
            return self.__upload_status_map[study_id].values()
        return []

    def get_all_files(self):
        out = []
        for study_id in self.get_studies():
            out = out + self.get_files(study_id)
        return out

    def print_upload_states(self):
        for file_upload in self.get_all_files():
            print("{}\t{}\t{}".format(file_upload.study_id, file_upload.upload_state , file_upload.filename ))




@unique
class FileUploadState(Enum):
    UPLOAD_ERROR = -6
    STATUS_ERROR = -5
    VALIDATION_ERROR = -4
    SAVE_ERROR = -3
    PUBLISH_ERROR = -2
    UNKNOWN_ERROR = -1
    NOT_UPLOADED = 0
    SUBMITTED = 1
    VALIDATED = 2
    SAVED = 3
    PUBLISHED = 4

    def __equals(self, other):
        return self.value == other.value

    def __lt__(self, other):
        utils.check_type(other, FileUploadState)
        return self.value < other.value

    def __gt__(self, other):
        return other.__lt__(self)

    def __eq__(self, other):
        utils.check_type(other, FileUploadState)
        return self.__equals(other)

    def __le__(self, other):
        return self.__lt__(other) or self.__equals(other)

    def __ge__(self, other):
        return other.__le__(self)

    def __str__(self):
        return super(FileUploadState, self).__str__().split('.')[1]


class FileUploadClient(object):

    def __init__(self, api, filename, is_async_validation=False, ignore_analysis_id_collisions=False):

        # Dependencies
        self.__api = api
        self.filename = filename
        self.study_id = api.config.study_id

        # Config
        self.is_async_validation = is_async_validation
        self.ignore_analysis_id_collisions = ignore_analysis_id_collisions
        self.retry_period_seconds = 0.1

        # State
        self.upload_state = FileUploadState.NOT_UPLOADED
        self.upload_status = None
        self.upload_id = None
        self.upload_errors = None
        self.analysis_id = None

        # Check
        utils.check_type(api, Api)
        utils.check_file(filename)

    def upload(self):
        if self.upload_state > FileUploadState.NOT_UPLOADED:
            log.warn("The file '{}' has already been uploaded".format(self.filename))
        else:
            with open(self.filename, 'r') as file_content:
                json_data = json.load(file_content)  # just to validate the json
            try:
                upload_response = self.__api.upload(json_data, is_async_validation=self.is_async_validation)
                utils.check_song_state(upload_response.status == 'ok' or 'WARNING' in upload_response.status, 'file.upload.fail',
                                       "The upload for file '{}' was unsuccessful", self.filename)
                self.upload_state = FileUploadState.SUBMITTED
                self.upload_id = upload_response.uploadId
            except SongClientException as se:
                self.upload_errors = "[SONG_CLIENT_EXCEPTION] {} @ {} : {}".format(se.id, se.timestamp, se.message)
                self.upload_state = FileUploadState.UPLOAD_ERROR
            except SongError as ex:
                self.upload_errors = ex
                self.upload_state = FileUploadState.UPLOAD_ERROR
            except Exception as e:
                self.upload_errors = "[{}] : ".format(e.__class__.__name__, e.message)
                self.upload_state = FileUploadState.UNKNOWN_ERROR

    def update_status(self):
        if self.upload_state == FileUploadState.VALIDATION_ERROR:
            log.error("Validation error for file '{}' with upload_id '{}': {}".format(self.filename,
                                                                                      self.upload_id,
                                                                                      self.upload_errors))
        elif self.upload_state == FileUploadState.NOT_UPLOADED:
            log.warn("Status undefined for file '{}' as it was not uploaded".format(self.filename))
        elif self.upload_state == FileUploadState.SUBMITTED:
            # actually calculate the status
            try:
                status_response = self.__api.status(self.upload_id)
                while status_response.state == 'CREATED' or status_response.state == 'UPDATED':
                    status_response = self.__api.status(self.upload_id)
                    time.sleep(self.retry_period_seconds)
                if status_response.state == 'VALIDATED':
                    self.upload_state = FileUploadState.VALIDATED
                elif status_response.state == 'SAVED':
                    self.upload_state = FileUploadState.SAVED
                elif status_response.state == 'PUBLISHED':
                    self.upload_state = FileUploadState.PUBLISHED
                elif status_response.state == 'VALIDATION_ERROR':
                    self.upload_state = FileUploadState.VALIDATION_ERROR
                    self.upload_errors = status_response.errors
                else:
                    self.upload_state = FileUploadState.UNKNOWN_ERROR
            except SongClientException as se:
                self.upload_errors = "[SONG_CLIENT_EXCEPTION] {} @ {} : {}".format(se.id, se.timestamp, se.message)
                self.upload_state = FileUploadState.STATUS_ERROR
            except SongError as ex:
                self.upload_errors = ex
                self.upload_state = FileUploadState.STATUS_ERROR
            except Exception as e:
                self.upload_errors = "[{}] : ".format(e.__class__.__name__, e.message)
                self.upload_state = FileUploadState.UNKNOWN_ERROR

        elif FileUploadState.VALIDATED < self.upload_state < FileUploadState.PUBLISHED:
            log.info(
                "The file '{}' with upload_id '{}' has already been validated and has state '{}'".format(
                    self.filename, self.upload_id, self.upload_state.__class__.__name__ ))

    def save(self):
        utils.check_state(self.upload_state >= FileUploadState.VALIDATED,
                          "Need to VALIDATE upload_id '{}' for file '{}' before SAVING",
                          self.upload_id, self.filename)
        if self.upload_state >= FileUploadState.SAVED:
            log.warn("The file '{}' with upload_id '{}' was already saved with analysis_id '{}'".format(
                self.filename, self.upload_id, self.analysis_id))
        else:
            try:
                save_response = self.__api.save(self.upload_id,
                                                ignore_analysis_id_collisions=self.ignore_analysis_id_collisions)
                utils.check_state(save_response.status == 'ok',
                                  "The save for upload_id '{}' for file '{}' was unsuccessfull: {}",
                                  self.upload_id, self.filename, save_response.__dict__ )
                self.upload_state = FileUploadState.SAVED
                self.analysis_id = save_response.analysisId
            except SongClientException as se:
                self.upload_errors = "[SONG_CLIENT_EXCEPTION] {} @ {} : {}".format(se.id, se.timestamp, se.message)
                self.upload_state = FileUploadState.SAVE_ERROR
            except SongError as ex:
                self.upload_errors = ex
                self.upload_state = FileUploadState.SAVE_ERROR
            except Exception as e:
                self.upload_errors = "[{}] : ".format(e.__class__.__name__, e.message)
                self.upload_state = FileUploadState.UNKNOWN_ERROR

    def publish(self):
        utils.check_state(self.upload_state >= FileUploadState.SAVED,
                          "Need to SAVE upload_id '{}' for file '{}' before PUBLISHING",
                          self.upload_id, self.filename)
        if self.upload_state >= FileUploadState.PUBLISHED:
            log.warn("The file '{}' with upload_id '{}' was already published with analysis_id '{}'".format(
                self.filename, self.upload_id, self.analysis_id))
        else:
            try:
                publish_response = self.__api.publish(self.analysis_id)
                utils.check_state(publish_response.status == 'ok',
                                  "The publish for analysis_id '{}' for file '{}' and upload_id '{}' was unsuccessfull: {}",
                                  self.analysis_id, self.filename, self.upload_id,  publish_response.__dict__)
                self.upload_state = FileUploadState.PUBLISHED
            except SongClientException as se:
                self.upload_errors = "[SONG_CLIENT_EXCEPTION] {} @ {} : {}".format(se.id, se.timestamp, se.message)
                self.upload_state = FileUploadState.PUBLISH_ERROR
            except SongError as ex:
                self.upload_errors = ex
                self.upload_state = FileUploadState.PUBLISH_ERROR
            except Exception as e:
                self.upload_errors = "[{}] : ".format(e.__class__.__name__, e.message)
                self.upload_state = FileUploadState.UNKNOWN_ERROR

