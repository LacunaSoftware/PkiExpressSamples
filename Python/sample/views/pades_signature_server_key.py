"""

This sample performs a local PAdES signature in one step using PKI Express.

"""
import os
import uuid

from flask import render_template
from flask import current_app
from flask import make_response
from flask import Blueprint
from pkiexpress import standard_signature_policies
from pkiexpress import PadesSigner

from sample.utils import set_pki_defaults
from sample.utils import get_pdf_stamp_path
from sample.utils import create_app_data
from sample.utils import get_expired_page_headers
from sample.utils_pades import get_visual_representation

blueprint = Blueprint('pades_signature_server_key', __name__,
                      url_prefix='/pades-signature-server-key')


@blueprint.route('/<userfile>')
def index(userfile):

    try:

        # Verify if the provided userfile exists.
        if not os.path.exists(os.path.join(current_app.config['APPDATA_FOLDER'],
                                           userfile)):
            return render_template('error.html', msg='File not found')

        # Get an instance of the PadesSigner class, responsible for receiving
        # the signature elements and performing the local signature.
        signer = PadesSigner()

        # Set PKI default options (see utils.py).
        set_pki_defaults(signer)

        # Set signature policy.
        signer.signature_policy = \
            standard_signature_policies.PADES_BASIC_WITH_LTV

        # Set PDF to be signed.
        signer.set_pdf_to_sign_from_path(os.path.join(
            current_app.config['APPDATA_FOLDER'], userfile))

        # The PKCS #12 certificate path.
        signer.set_pkcs12_from_path(os.path.join(current_app.static_folder,
                                                 'Pierre de Fermat.pfx'))
        # Set the certificate's PIN.
        signer.cert_password = '1234'

        # Set a file reference for the stamp file. Note that this file can be
        # referenced later by "fref://{alias}" at the "url" field on the visual
        # representation (see content/vr.json or get_visual_representation()
        # method).
        signer.add_file_reference('stamp', get_pdf_stamp_path())

        # Set visual representation. We provide a dictionary that represents the
        # visual representation JSON model.
        signer.set_visual_representation(get_visual_representation())

        # Generate path for output file and add to signer object.
        create_app_data()  # Guarantees that "app data" folder exists.
        output_file = '%s.pdf' % (str(uuid.uuid4()))
        signer.output_file = os.path.join(current_app.config['APPDATA_FOLDER'],
                                          output_file)

        # Perform the signature.
        signer.sign()

        response = make_response(render_template(
            'pades_signature_server_key/index.html', filename=output_file))
        response.headers = get_expired_page_headers()
        return response

    except Exception as e:
        return render_template('error.html', msg=e)
