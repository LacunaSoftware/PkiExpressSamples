import os

from datetime import datetime
from datetime import timedelta
from flask import current_app
from pkiexpress import TimestampAuthority


def set_pki_defaults(operator):
    # If you want to operator to trust in a custom trusted root, you need to
    # inform the operator class. You can trust on more than one roots by
    # uncommenting the following lines:
    # operator.add_trusted_root(path_one)
    # operator.add_trusted_root(path_two)
    # operator.add_trusted_root(path_three)

    # If you want the operator to trust on Lacuna Test Root (default: false),
    # uncomment the following line:
    # operator.trust_lacuna_test_root = True

    # If you want the operator to perform its action on "OFFLINE MODE"
    # (default: False), uncomment the following line:
    # operator.offline = True

    # If you want to perform a signature with timestamp, set the timestamp
    # authority. You can use REST PKI to do this (acquire access token on
    # https://pki.rest), by commenting the following lines:
    # tsa = TimestampAuthority('https://pki.rest/tsp/a402df41-8559-47b2-a05c-be555bf66310')
    # tsa.set_oauth_token_authentication('SET YOU ACCESS TOKEN HERE')
    # operator.timestamp_authority = tsa
    pass


def get_expired_page_headers():
    headers = dict()
    now = datetime.utcnow()
    expires = now - timedelta(seconds=3600)

    headers['Expires'] = expires.strftime("%a, %d %b %Y %H:%M:%S GMT")
    headers['Last-Modified'] = now.strftime("%a, %d %b %Y %H:%M:%S GMT")
    headers['Cache-Control'] = 'private, no-store, max-age=0, no-cache,' \
                               ' must-revalidate, post-check=0, pre-check=0'
    headers['Pragma'] = 'no-cache'
    return headers


def create_app_data():
    if not os.path.exists(current_app.config['APPDATA_FOLDER']):
        os.makedirs(current_app.config['APPDATA_FOLDER'])


def get_pdf_stamp_path():
    return os.path.join(current_app.static_folder, 'stamp.png')


def get_sample_doc_path():
    return os.path.join(current_app.static_folder, 'SampleDocument.pdf')

def get_sample_batch_doc_path(file_id):
    return '%s/%02d.pdf' % (current_app.static_folder, (int(file_id) % 10))
