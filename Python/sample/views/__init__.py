from .cades_signature import blueprint as cades_signature
from .cades_signature_server_key import blueprint as cades_signature_server_key
from .download import blueprint as download
from .home import blueprint as home
from .pades_signature import blueprint as pades_signature
from .pades_signature_server_key import blueprint as pades_signature_server_key
from .upload import blueprint as upload

blueprints = {
    cades_signature,
    cades_signature_server_key,
    download,
    home,
    pades_signature,
    pades_signature_server_key,
    upload,
}
