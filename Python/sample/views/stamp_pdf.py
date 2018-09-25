import os
import uuid

from flask import abort
from flask import Blueprint
from flask import current_app
from flask import send_from_directory
from pkiexpress import PadesTimestamper

from sample.utils import set_pki_defaults
from sample.utils import create_app_data

blueprint = Blueprint('stamp_pdf', __name__, url_prefix='/stamp-pdf')


@blueprint.route('/<filename>')
def get(filename):

    # Locate document. This sample should not ocntinue if the file is not found.
    if not os.path.exists(os.path.join(current_app.config['APPDATA_FOLDER'],
                                       filename)):
        # Return "Not found" code.
        abort(404)
        return

    # Get an instance of the PadesTimestamper class, used to timestamp a PDF
    # file.
    stamper = PadesTimestamper()

    # Set PKI default (see utils.py).
    set_pki_defaults(stamper)

    # Set the PDF to be timestamped.
    stamper.set_pdf_from_path(os.path.join(current_app.config['APPDATA_FOLDER'],
                                                              filename))

    # Generate path for output file and add to the stamper.
    create_app_data() # Guarantees that "app_data" folder exists.
    output_file = '%s.pdf' % str(uuid.uuid4())
    stamper.output_file_path = os.path.join(
        current_app.config['APPDATA_FOLDER'], output_file)

    # Add a timestmap to the PDF file.
    stamper.stamp()

    # Return the stamped PDF as a downloadable file.
    return send_from_directory(current_app.config['APPDATA_FOLDER'],
                               output_file)
