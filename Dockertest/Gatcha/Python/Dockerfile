# Use an official Python runtime as the base image
FROM python:3.9

# Set the working directory in the container
WORKDIR /app

# Copy the requirements file to the working directory
COPY requirements.txt .

# Install the Python dependencies
RUN pip install --no-cache-dir fastapi uvicorn pymongo passlib python-jose sqlalchemy datetime pprintpp  databases python-multipart

# Copy the rest of the application code to the working directory
COPY . .

# Set the command to run when the container starts
CMD ["uvicorn", "test:app", "--host", "0.0.0.0", "--port", "80"]

#RUN sleep 10 && python init_mongo.py