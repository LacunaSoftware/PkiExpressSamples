from .download import blueprint as download
from .home import blueprint as home
from .pades_signature import blueprint as pades_signature
from .pades_signature_server_key import blueprint as pades_signature_server_key
from .upload import blueprint as upload

blueprints = {
    download,
    home,
    pades_signature,
    pades_signature_server_key,
    upload,
}
