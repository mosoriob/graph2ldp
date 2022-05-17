from configparser import ConfigParser
import os
from pathlib import Path

# Default endpoint, if none specified elsewhere
ENDPOINT = os.environ.get('ENDPOINT')
ENDPOINT_USERNAME = os.environ.get('USER')
ENDPOINT_PASSWORD = os.environ.get('PASSWORD')
ENDPOINT_RESOURCE_PREFIX = os.environ.get('PREFIX')
ENDPOINT_GRAPH_BASE = os.environ.get('GRAPH_BASE')
FIREBASE_KEY = os.environ.get('FIREBASE_KEY')
QUERY_DIRECTORY = os.environ.get('QUERIES_DIR')
CONTEXT_DIRECTORY = os.environ.get('CONTEXT_DIR')
AUTH_SERVER = os.environ.get('AUTH_SERVER')
AUTH_CLIENT_ID = os.environ.get('AUTH_CLIENT_ID')

mime_types = {
    'csv': 'text/csv; q=1.0, */*; q=0.1',
    'json': 'application/json; q=1.0, application/sparql-results+json; q=0.8, */*; q=0.1',
    'html': 'text/html; q=1.0, */*; q=0.1',
    'ttl': 'text/turtle'
}

UPDATE_ENDPOINT = f'{ENDPOINT}/update'
QUERY_ENDPOINT = f'{ENDPOINT}/query'

QUERIES_TYPES = ["get_all", "get_all_related", "get_all_related_user", "get_all_user", "get_one", "get_one_user"]

logging_file = Path(__file__).parent / "logging.ini"
