"""

This sample performs a CAdES signature in three steps using PKI Express and
Web PKI.

"""
import os
import uuid

from flask import Blueprint
from flask import current_app
from flask import render_template
from flask import flash
from flask import redirect
from flask import url_for
from flask import request
from pkiexpress import CadesSignatureStarter
from pkiexpress import SignatureFinisher
from pkiexpress import standard_signature_policies

from sample.utils import set_pki_defaults
from sample.utils import create_app_data
from sample.utils import get_sample_doc_path

blueprint = Blueprint('cades_signature', __name__,
                      url_prefix='/cades-signature')


@blueprint.route('/')
@blueprint.route('/<userfile>')
def index(userfile=None):
    """

    This method only renders the signature page.

    """
    # Verify if the provided userfile exists.
    if userfile and not os.path.exists(os.path.join(
            current_app.config['APPDATA_FOLDER'], userfile)):
        return render_template('error.html', msg='File not found')

    return render_template('cades_signature/index.html',
                           userfile=userfile)


@blueprint.route('/start', methods=['POST'])
def start():
    """

    This method starts the signature. In this sample, it will be called
    programatically after the user press the "Sign File" button (see method
    readCertificate() on static/js/signature-start-form.js).

    """
    userfile = None
    try:
        # Recover variables from the POST arguments to be used on this step.
        cert_thumb = request.form['certThumbField']
        cert_content = request.form['certContentField']
        if request.form['userfileField'] != 'None':
            userfile = request.form['userfileField']

        # Get an instance of the CadesSignatureStarter class, responsible for
        # receiving the signature elements and start the signature process.
        signature_starter = CadesSignatureStarter()

        # Set PKI default options (see utils.py).
        set_pki_defaults(signature_starter)

        # Set signature policy.
        signature_starter.signature_policy = \
            standard_signature_policies.PKI_BRAZIL_CADES_ADR_BASICA

        # Set file to be signed. If the file is a CMS, PKI Express will
        # recognize that and will co-sign that file.
        if userfile:
            signature_starter.set_file_to_sign_from_path(
                os.path.join(current_app.config['APPDATA_FOLDER'], userfile))
        else:
            signature_starter.set_file_to_sign_from_path(get_sample_doc_path())

        # Set Base64-encoded certificate's content to signature starter.
        signature_starter.set_certificate_from_base64(cert_content)

        # Set 'encapsulated content' option (default: True).
        signature_starter.encapsulated_content = True

        # Start the signature process. Receive as response the following fields:
        # - to_sign_hash:     The hash to be signed.
        # - digest_algorithm: The digest algorithm that will inform the Web PKI
        #                     component to compute the signature.
        # - transfer_file:    A temporary file to be passed to "complete" step.
        response = signature_starter.start()

        # Render the field from start() method as hidden fields to be used on
        # the javascript or on the "complete" step.
        return render_template('cades_signature/start.html',
                               to_sign_hash=response['toSignHash'],
                               digest_algorithm=response['digestAlgorithm'],
                               transfer_file=response['transferFile'],
                               cert_thumb=cert_thumb,
                               userfile=userfile)

    except Exception as e:
        flash(str(e))
        return redirect(url_for('cades_signature.index', userfile=userfile))


@blueprint.route('/complete', methods=['POST'])
def complete():
    """

    This function completes the signature, it will be called programatically
    after the Web PKI component perform the signature and submit the form (see
    method sign() on static/js/signature-complete-form.js).

    """
    userfile = None
    try:

        # Recover variables from the POST arguments to be used on this step.
        transfer_file = request.form['transferFileField']
        signature = request.form['signatureField']
        if request.form['userfileField'] != 'None':
            userfile = request.form['userfileField']

        # Get an instance of the SignatureFinisher class, responsible for
        # completing the signature process.
        signature_finisher = SignatureFinisher()

        # Set PKI default options (see utils.py).
        set_pki_defaults(signature_finisher)

        # Set the file to be signed. It's the same file we used on "start"
        # method.
        if userfile:
            signature_finisher.set_file_to_sign_from_path(
                os.path.join(current_app.config['APPDATA_FOLDER'], userfile))
        else:
            signature_finisher.set_file_to_sign_from_path(get_sample_doc_path())

        # Set the transfer file.
        signature_finisher.set_transfer_file_from_path(transfer_file)

        # Set the signature file.
        signature_finisher.signature = signature

        # Generate path for output file and add to the signature finisher.
        create_app_data()  # Guarantees that "app data" folder exists.
        filename = '%s.p7s' % (str(uuid.uuid4()))
        signature_finisher.output_file = \
            os.path.join(current_app.config['APPDATA_FOLDER'], filename)

        # Complete the signature process.
        signature_finisher.complete()

        return render_template('cades_signature/signature-info.html',
                               filename=filename)

    except Exception as e:
        flash(str(e))
        return redirect(url_for('cades_signature.index', userfile=userfile))
