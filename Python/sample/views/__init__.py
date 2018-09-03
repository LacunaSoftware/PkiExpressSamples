from .batch_cades_signature import blueprint as batch_cades_signature
from .batch_pades_signature import blueprint as batch_pades_signature
from .cades_signature import blueprint as cades_signature
from .cades_signature_server_key import blueprint as cades_signature_server_key
from .download import blueprint as download
from .home import blueprint as home
from .pades_signature import blueprint as pades_signature
from .pades_signature_server_key import blueprint as pades_signature_server_key
from .upload import blueprint as upload

blueprints = {
    batch_cades_signature,
    batch_pades_signature,
    cades_signature,
    cades_signature_server_key,
    download,
    home,
    pades_signature,
    pades_signature_server_key,
    upload,
}


