"""

This sample performs a batch of CAdES signature using PKI Express and Web PKI

"""
import os
import uuid

from flask import Blueprint
from flask import current_app
from flask import jsonify
from flask import render_template
from flask import request
from pkiexpress import CadesSignatureStarter
from pkiexpress import SignatureFinisher
from pkiexpress import standard_signature_policies

from sample.utils import set_pki_defaults
from sample.utils import create_app_data
from sample.utils import get_sample_batch_doc_path

blueprint = Blueprint('batch_cades_signature', __name__,
                      url_prefix='/batch-cades-signature')

@blueprint.route('/')
def index():
    """

    This function renders the batch signature page.

    Notice that the only thing we'll do on the server-side at this point is
    determine the IDs of the documents to be signed. The page will handle
    each document one by one and will call the server asynchronously to start
    and complete each signature.

    """

    # It is up to your application's business logic to determine which documents
    # will compose the batch.
    document_ids = list(range(1, 31))

    # Render the batch signature page.
    return render_template('batch_cades_signature/index.html',
                           document_ids=document_ids)


@blueprint.route('/start', methods=['POST'])
def start():
    """

    This method is called asynchronously via AJAX by the batch signature page
    for each document being signed. It initiates a CAdES signature using
    PKI Express and returns a JSON with the values to be used in the next
    signature steps (see batch-signature-form.js).

    """

    # Recover variables from the POST arguments to be used in this step.
    file_id = request.form['id']
    cert_content = request.form['certContent']

    # Get an instance of the CadesSignatureStarter class, responsible for
    # receiving the signature elements and start the signature process.
    signature_starter = CadesSignatureStarter()

    # Set PKI default options (see utils.py).
    set_pki_defaults(signature_starter)

    # Set signature policy.
    signature_starter.signature_policy = \
        standard_signature_policies.PKI_BRAZIL_CADES_ADR_BASICA

    # Set file to be signed based on its ID.
    signature_starter.set_file_to_sign_from_path(
        get_sample_batch_doc_path(file_id))

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

    return jsonify(response)

@blueprint.route('/complete', methods=['POST'])
def complete():
    """

    This method is called asynchronously via AJAX by the batch signature page
    for each document being signed. It completes the CAdES signature using
    PKI Express and returns a JSON with the saved filename so that the page can
    render a link to it.

    """

    # Recover variables from the POST arguments to be used on this step.
    file_id = request.form['id']
    transfer_file = request.form['transferFile']
    signature = request.form['signature']

    # Get an instance of the SignatureFinisher class, responsible for completing
    # the signature process.
    signature_finisher = SignatureFinisher()

    # Set the file to be signed. It's the same file we use don "start" method.
    signature_finisher.set_file_to_sign_from_path(
        get_sample_batch_doc_path(file_id))

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

    return jsonify(filename)