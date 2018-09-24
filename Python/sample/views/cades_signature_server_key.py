"""

This sample performs a local CAdES signature in one step usign PKI Express.

"""
import os
import uuid

from flask import Blueprint, render_template, current_app, make_response
from pkiexpress import CadesSigner, standard_signature_policies

from sample.utils import set_pki_defaults, create_app_data, \
    get_expired_page_headers

blueprint = Blueprint('cades_signature_server_key', __name__,
                      url_prefix='/cades-signature-server-key')


@blueprint.route('/<userfile>')
def index(userfile):

    try:

        # Verify if the provided userfile exists.
        if not os.path.exists(os.path.join(current_app.config['APPDATA_FOLDER'],
                                           userfile)):
            return render_template('error.html', msg='File not found')

        # Get an instance of the CadesSigner class, responsible for receiving
        # the signature elements and performing the local signature.
        signer = CadesSigner()

        # Set PKI default options (see utils.py).
        set_pki_defaults(signer)

        # Set signature policy.
        signer.signature_policy = \
            standard_signature_policies.PKI_BRAZIL_CADES_ADR_BASICA

        # Set file to be signed. If the file is a CSM, the PKI Express will
        # recognize that and will co-sign that file.
        signer.set_file_to_sign_from_path(os.path.join(
            current_app.config['APPDATA_FOLDER'], userfile))

        # The PKCS #12 certificate path.
        signer.set_pkcs12_from_path(os.path.join(current_app.static_folder,
                                                 'Pierre de Fermat.pfx'))
        # Set the certificate's PIN.
        signer.cert_password = '1234'

        # Set 'encapsulate content' option (default: True).
        signer.encapsulated_content = True

        # Generate path for output file and add to signer object.
        create_app_data()  # Guarantees that "app_data" folder exists.
        output_file = '%s.p7s' % (str(uuid.uuid4()))
        signer.output_file = os.path.join(current_app.config['APPDATA_FOLDER'],
                                          output_file)

        # Perform the signature.
        signer_cert = signer.sign(get_cert=True)

        response = make_response(render_template(
            'cades_signature_server_key/index.html',
            signer_cert=signer_cert,
            filename=output_file))
        response.headers = get_expired_page_headers()
        return response

    except Exception as e:
        return render_template('error.html', msg=e)