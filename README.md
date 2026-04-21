# Hazardous Waste Thermal Treatment Mini Program

[中文说明](./README.zh-CN.md)

Sanitized public portfolio version of a full-stack WeChat Mini Program project built for an environmental science workflow.

## Overview

This project combines a WeChat Mini Program frontend with a Spring Boot backend to support hazardous waste information management, thermal-property browsing, and compatibility simulation.

## Repository Structure

- `frontend/`: WeChat Mini Program client
- `backend/`: Spring Boot backend service

## Core Features

- Hazardous waste directory lookup and detail pages
- Physical-property query and categorized data display
- Thermal-property browsing with spectrum and image preview support
- Compatibility checking and blending simulation workflow
- REST APIs for search, upload, calculation, and result retrieval

## Tech Stack

- Frontend: WeChat Mini Program native framework
- Backend: Java 11, Spring Boot, MyBatis-Plus, MySQL
- API docs: Springdoc OpenAPI

## Sanitization Notes

- This repository is a sanitized showcase version rather than the original deployment repository.
- Real server addresses, database credentials, app secrets, uploaded files, and private environment configuration have been removed or replaced with placeholders.
- Build artifacts, logs, IDE files, and local-only configuration are excluded.

## Local Setup

1. Update backend configuration with your own database and Mini Program environment values.
2. Update `frontend/config/api-config.js` to point to your backend service.
3. Import `frontend/` into WeChat DevTools.
4. Run the Spring Boot backend from `backend/`.

## Intended Use

This repository is suitable for portfolio display, technical review, and project experience reference.
