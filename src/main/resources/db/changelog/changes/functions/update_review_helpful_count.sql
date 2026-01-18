CREATE OR REPLACE FUNCTION update_review_helpful_count()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        UPDATE reviews
        SET helpful_count = helpful_count + 1
        WHERE id = NEW.review_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE reviews
        SET helpful_count = helpful_count - 1
        WHERE id = OLD.review_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;
