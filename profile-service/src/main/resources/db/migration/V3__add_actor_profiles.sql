ALTER TABLE doctors ADD COLUMN auth_user_id UUID;
ALTER TABLE doctors ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE doctors ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE doctors ADD CONSTRAINT uk_doctor_auth_user UNIQUE (auth_user_id);

ALTER TABLE insured_persons ADD COLUMN created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();

CREATE TABLE social_agents (
    id UUID PRIMARY KEY,
    auth_user_id UUID NOT NULL,
    first_name VARCHAR(80) NOT NULL,
    last_name VARCHAR(80) NOT NULL,
    username VARCHAR(80) NOT NULL,
    phone_number VARCHAR(40),
    email VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_social_agent_auth_user UNIQUE (auth_user_id),
    CONSTRAINT uk_social_agent_email UNIQUE (email)
);
